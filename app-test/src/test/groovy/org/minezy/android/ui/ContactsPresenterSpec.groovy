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
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func0;


class ContactsPresenterSpec extends RoboSpecification {

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
    def view = Mock(ContactsView)
    def ContactsPresenter presenter = new ContactsPresenter()
    def Observable webViewFinished = Observable.just(null);

    def setup() {
        module.context = Robolectric.application
        module.apiV1 = Mock(MinezyApiV1)
        module.sharedPreferences = Mock(SharedPreferences)

        module.sharedPreferences.getString('account_email', _) >> "pete.davis@enron.com"
        module.apiV1.getContacts('pete.davis@enron.com') >> contacts
        view.getContext() >> module.context
        view.getWebViewFinished() >> webViewFinished

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
        presenter.onCreate(view)

        then:
        1 * module.apiV1.getContacts({
            ImmediateTestScheduler.sCurrent == module.ioScheduler
        }) >> contacts
    }


    def "onCreate() should set contacts to view controller"() {
        when:
        presenter.onCreate(view)

        then:
        1 * view.setContacts(contacts);
    }

    def "onCreate() calls view controller on main thread"() {
        when:
        presenter.onCreate(view)

        then:
        1 * view.setContacts({
            ImmediateTestScheduler.sCurrent == module.mainScheduler
        });
    }

    def "onCreate() sets contacts' graph data to view controller"() {
        when:
        presenter.onCreate(view)

        then:
        1 * view.setWebviewData(_);
    }

    def "onCreate() sets contacts' graph data to view controller that is valid json"() {
        when:
        presenter.onCreate(view)

        then:
        1 * view.setWebviewData({ data -> new JSONObject(data) });
    }

    def "onCreate() sets contacts' graph data to view controller that matches expected json"() {
        when:
        presenter.onCreate(view)

        then:
        1 * view.setWebviewData({ data ->
            JSONAssert.assertEquals(contactsGraphJson, data, false)
            true
        })
    }

    def "onCreate() doesn't set contacts' graph when web view is not ready"() {
        when:
        webViewFinished = Observable.never();

        presenter.onCreate(view)

        then:
        1 * view.setWebviewData(_);
    }

}