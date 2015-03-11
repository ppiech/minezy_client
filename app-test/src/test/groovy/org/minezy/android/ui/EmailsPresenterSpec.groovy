package org.minezy.android.ui

import android.content.Intent
import android.content.SharedPreferences
import dagger.ObjectGraph
import org.minezy.android.TestModule
import org.minezy.android.data.MinezyApiV1
import org.minezy.android.model.Contact
import org.minezy.android.model.Email
import org.minezy.android.utils.ImmediateTestScheduler
import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification

class EmailsPresenterSpec extends RoboSpecification {

    final List<Contact> emails = [
            new Email("5964974.1075840259917.JavaMail.evans@thyme", new Date("Wed, 13 Sep 2000 14:42:00 -0700 (PDT)"), "Talking points from Dave Walden"),
            new Email("13123665.1075840236503.JavaMail.evans@thyme", new Date("Wed, 13 Sep 2000 14:42:00 -0700 (PDT)"), "Talking points from Dave Walden"),
            new Email("32558075.1075840209050.JavaMail.evans@thyme", new Date("Wed, 13 Sep 2000 14:42:00 -0700 (PDT)"), "Talking points from Dave Walden")
    ]

    def module = new TestModule()
    def view = Mock(EmailsView)
    def presenter = new EmailsPresenter()
    def intent

    def setup() {
        module.context = Robolectric.application
        module.apiV1 = Mock(MinezyApiV1)
        module.sharedPreferences = Mock(SharedPreferences)

        module.sharedPreferences.getString('account_email', _) >> "pete.davis@enron.com"

        view.getContext() >> module.context
        module.apiV1.getEmails("pete.davis@enron.com", "jeff.dasovich@enron.com") >> emails
        intent = new Intent(module.context, EmailsActivity.class);
        intent.putExtra("contact", "jeff.dasovich@enron.com");
        presenter = new EmailsPresenter()

        ObjectGraph.create(module).inject(presenter)
    }

    def "onCreate() retrieves emails for contact from intent extra EmailsActivity.ARG_CONTACT"() {
        when:
        presenter.onCreate(view, intent)

        then:
        1 * module.apiV1.getEmails("pete.davis@enron.com", "jeff.dasovich@enron.com") >> emails
    }

    def "onCreate() retrieves emails on background thread"() {
        when:
        presenter.onCreate(view, intent)

        then:
        1 * module.apiV1.getEmails({ ImmediateTestScheduler.sCurrent == module.ioScheduler }, _) >> emails
    }


    def "onCreate() should set email to view controller"() {
        when:
        presenter.onCreate(view, intent)

        then:
        1 * view.setEmails(emails);
    }

    def "onCreate() calls view controller on main thread"() {
        when:
        presenter.onCreate(view, intent)

        then:
        1 * view.setEmails({ ImmediateTestScheduler.sCurrent == module.mainScheduler });
    }

}