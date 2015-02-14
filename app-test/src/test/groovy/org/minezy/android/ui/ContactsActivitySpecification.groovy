package org.minezy.android.ui

import android.content.Context
import android.content.SharedPreferences
import org.minezy.android.data.MinezyApiV1
import org.minezy.android.model.Contact
import org.minezy.android.utils.AsyncTaskUtil
import pl.polidea.robospock.RoboSpecification

class ContactsActivitySpecification extends RoboSpecification {

    def apiV1 = Mock(MinezyApiV1)
    def asyncTaskUtil = Mock(AsyncTaskUtil)
    def controller = Mock(ContactsActivityController)
    def context = Mock(Context)
    def sharedPreferences = Mock(SharedPreferences)
    def presenter

    def "getContacts() should return list of contacts"() {
        given:
        final List<Contact> contacts = [
                new Contact("pete.davis@enron.com", "Pete Davis"),
                new Contact("vince.kaminski@enron.com", "Vince J Kaminski"),
                new Contact("jeff.dasovich@enron.com", "Jeff Dasovich")
        ]

        sharedPreferences.getString("account_email") >> "pete.davis@enron.com"
        controller.getContext() >> context
        presenter = new ContactsActivityPresenter(controller, asyncTaskUtil, apiV1, sharedPreferences)
        apiV1.getContacts() >> contacts


        def captured
        def result

        when:
        presenter.onCreate()

        then:
        1 * asyncTaskUtil.execute({ it ->
            captured = it
        }, _)

//        captured.doInBackground("pete.davis@enron.com")
//
//        result == null
//        captured == null
//
//        print captured
//
//        final List<Contact> result = captured.doInBackground((String[]) [])
//        captured.onPostExecute(result)
//
//        1 * controller.setContacts(contacts)
    }


}