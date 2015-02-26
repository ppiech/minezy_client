package org.minezy.android.ui

import android.content.SharedPreferences
import dagger.ObjectGraph
import org.minezy.android.R
import org.minezy.android.TestModule
import org.minezy.android.data.MinezyApiV1
import org.minezy.android.model.Contact
import org.minezy.android.utils.TaskChainFactory
import org.minezy.android.utils.TestExecutor
import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification

class ContactsActivitySpecification extends RoboSpecification {

    final List<Contact> contacts = [
            new Contact("pete.davis@enron.com", "Pete Davis"),
            new Contact("vince.kaminski@enron.com", "Vince J Kaminski"),
            new Contact("jeff.dasovich@enron.com", "Jeff Dasovich")
    ]

    def module = new TestModule()
    def controller = Mock(ContactsActivityController)
    def mainExecutor = new TestExecutor();
    def backgroundExecutor = new TestExecutor();
    def presenter = new ContactsActivityPresenter()

    def setup() {
        module.context = Robolectric.application
        module.apiV1 = Mock(MinezyApiV1)
        module.sharedPreferences = Mock(SharedPreferences)
        module.taskChainFactory = new TaskChainFactory(mainExecutor, backgroundExecutor);

        module.sharedPreferences.getString('account_email', _) >> "pete.davis@enron.com"
        controller.getContext() >> module.context
        module.apiV1.getContactsWithLeft('pete.davis@enron.com') >> contacts

        ObjectGraph.create(module).inject(presenter)
    }

    def "Robolectric.application context should return strings from resources"() {
        when:
        def prefs_account_email = module.context.getString(R.string.pref_account_email)

        then:
        prefs_account_email == "account_email"
    }


    def "onCreate() retrieves contacts from apiV1 on background thread"() {
        when:
        presenter.onCreate(controller)

        then:
        1 * module.apiV1.getContactsWithLeft({ TestExecutor.executing() == backgroundExecutor })
    }


    def "onCreate() should set contacts to view controller"() {
        when:
        presenter.onCreate(controller)

        then:
        1 * controller.setContacts(contacts);
    }

    def "onCreate() calls view controller on main thread"() {
        when:
        presenter.onCreate(controller)

        then:
        1 * controller.setContacts({ TestExecutor.executing() == mainExecutor });
    }

}