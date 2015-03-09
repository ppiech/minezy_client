package org.minezy.android.ui

import android.content.SharedPreferences
import dagger.ObjectGraph
import org.json.JSONObject
import org.json.JSONTokener
import org.minezy.android.R
import org.minezy.android.TestModule
import org.minezy.android.data.MinezyApiV1
import org.minezy.android.model.Contact
import org.minezy.android.utils.ImmediateTestScheduler
import org.robolectric.Robolectric
import org.skyscreamer.jsonassert.JSONAssert
import pl.polidea.robospock.RoboSpecification

class ContactsActivitySpecification extends RoboSpecification {

    final List<Contact> contacts = [
            new Contact("pete.davis@enron.com", "Pete Davis"),
            new Contact("vince.kaminski@enron.com", "Vince J Kaminski"),
            new Contact("jeff.dasovich@enron.com", "Jeff Dasovich")
    ]

    final JSONObject contactsGraphJson = new JSONObject(new JSONTokener(
            """\
            {
            "nodes": [
                {"name":"Me","group":1},
                {"name":"Pete Davis","group":1},
                {"name":"Vince J Kaminski","group":1},
                {"name":"Jeff Dasovich","group":1} ],
            "links":[
                {"source":0,"target":1,"value":1},
                {"source":0,"target":2,"value":1},
                {"source":0,"target":3,"value":1} ]
            }
            """));

    def module = new TestModule()
    def controller = Mock(ContactsActivityController)
    def presenter = new ContactsActivityPresenter()

    def setup() {
        module.context = Robolectric.application
        module.apiV1 = Mock(MinezyApiV1)
        module.sharedPreferences = Mock(SharedPreferences)

        module.sharedPreferences.getString('account_email', _) >> "pete.davis@enron.com"
        controller.getContext() >> module.context
        module.apiV1.getContacts('pete.davis@enron.com') >> contacts

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
        1 * module.apiV1.getContacts({
            ImmediateTestScheduler.sCurrent == module.ioScheduler
        }) >> contacts
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
        1 * controller.setContacts({
            ImmediateTestScheduler.sCurrent == module.mainScheduler
        });
    }

    def "onCreate() sets contacts' graph data to view controller"() {
        when:
        presenter.onCreate(controller)

        then:
        1 * controller.setWebviewData(_);
    }

    def "onCreate() sets contacts' graph data to view controller that is valid json"() {
        when:
        presenter.onCreate(controller)

        then:
        1 * controller.setWebviewData({ data -> new JSONObject(data) });
    }

    def "onCreate() sets contacts' graph data to view controller that matches expected json"() {
        when:
        presenter.onCreate(controller)

        then:
        1 * controller.setWebviewData({ data ->
            JSONAssert.assertEquals(contactsGraphJson, data, false)
            true
        })
    }
}