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

    def apiV1 = Mock(MinezyApiV1)
    def controller = Mock(ContactsActivityController)
    def context = Robolectric.application
    def sharedPreferences = Mock(SharedPreferences)
    def mainExecutor = new TestExecutor();
    def backgroundExecutor = new TestExecutor();
    def taskChainFactory = new TaskChainFactory(mainExecutor, backgroundExecutor);

    def "Robolectric.application context should return strings from resources"() {
        when:
        def prefs_account_email = context.getString(R.string.pref_account_email)

        then:
        prefs_account_email == "account_email"
    }


    def "onCreate() should retrieve contacts in background and set them to view controller"() {
        given:
        final List<Contact> contacts = [
                new Contact("pete.davis@enron.com", "Pete Davis"),
                new Contact("vince.kaminski@enron.com", "Vince J Kaminski"),
                new Contact("jeff.dasovich@enron.com", "Jeff Dasovich")
        ]

        sharedPreferences.getString('account_email', _) >> "pete.davis@enron.com"
        controller.getContext() >> context
        def presenter = new ContactsActivityPresenter(controller, taskChainFactory, apiV1, sharedPreferences)

        when:
        presenter.onCreate()

        then:
        apiV1.getContactsWithLeft('pete.davis@enron.com') >> {
            assert TestExecutor.executing() == backgroundExecutor
            return contacts
        }
        1 * controller.setContacts({
            assert TestExecutor.executing() == mainExecutor
            it == contacts
        });
    }


}