package org.minezy.android.ui

import android.content.SharedPreferences
import org.minezy.android.R
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

    def context = Robolectric.application
    def apiV1 = Mock(MinezyApiV1)
    def controller = Mock(ContactsActivityController)
    def sharedPreferences = Mock(SharedPreferences)
    def mainExecutor = new TestExecutor();
    def backgroundExecutor = new TestExecutor();
    def taskChainFactory = new TaskChainFactory(mainExecutor, backgroundExecutor);

    def presenter

    def setup() {
        sharedPreferences.getString('account_email', _) >> "pete.davis@enron.com"
        controller.getContext() >> context
        presenter = new ContactsActivityPresenter(controller, taskChainFactory, apiV1, sharedPreferences)
        apiV1.getContactsWithLeft('pete.davis@enron.com') >> contacts
    }

    def "Robolectric.application context should return strings from resources"() {
        when:
        def prefs_account_email = context.getString(R.string.pref_account_email)

        then:
        prefs_account_email == "account_email"
    }


    def "onCreate() retrieves contacts from apiV1 on background thread"() {
        when:
        presenter.onCreate()

        then:
        1 * apiV1.getContactsWithLeft({ TestExecutor.executing() == backgroundExecutor })
    }


    def "onCreate() should set contacts to view controller"() {
        when:
        presenter.onCreate()

        then:
        1 * controller.setContacts(contacts);
    }

    def "onCreate() calls view controller on main thread"() {
        when:
        presenter.onCreate()

        then:
        1 * controller.setContacts({ TestExecutor.executing() == mainExecutor });
    }

}