package org.minezy.android.ui

import android.content.Intent
import android.content.SharedPreferences
import org.minezy.android.data.MinezyApiV1
import org.minezy.android.model.Contact
import org.minezy.android.model.Email
import org.minezy.android.utils.TaskChainFactory
import org.minezy.android.utils.TestExecutor
import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification

class EmailsActivitySpecification extends RoboSpecification {

    final List<Contact> emails = [
            new Email("5964974.1075840259917.JavaMail.evans@thyme", new Date("Wed, 13 Sep 2000 14:42:00 -0700 (PDT)"), "Talking points from Dave Walden"),
            new Email("13123665.1075840236503.JavaMail.evans@thyme", new Date("Wed, 13 Sep 2000 14:42:00 -0700 (PDT)"), "Talking points from Dave Walden"),
            new Email("32558075.1075840209050.JavaMail.evans@thyme", new Date("Wed, 13 Sep 2000 14:42:00 -0700 (PDT)"), "Talking points from Dave Walden")
    ]

    def context = Robolectric.application
    def apiV1 = Mock(MinezyApiV1)
    def controller = Mock(EmailsActivityController)
    def sharedPreferences = Mock(SharedPreferences)
    def mainExecutor = new TestExecutor();
    def backgroundExecutor = new TestExecutor();
    def taskChainFactory = new TaskChainFactory(mainExecutor, backgroundExecutor);
    def intent = new Intent(context, EmailsActivity.class);

    def presenter

    def setup() {
        sharedPreferences.getString('account_email', _) >> "pete.davis@enron.com"
        controller.getContext() >> context
        apiV1.getEmailsWithLeftAndRight("pete.davis@enron.com", "jeff.dasovich@enron.com") >> emails
        intent.putExtra("contact", "jeff.dasovich@enron.com");
        presenter = new EmailsActivityPresenter(controller, intent, taskChainFactory, apiV1, sharedPreferences)
    }

    def "onCreate() retrieves emails for contact from intent extra EmailsActivity.ARG_CONTACT"() {
        when:
        presenter.onCreate()

        then:
        1 * apiV1.getEmailsWithLeftAndRight("pete.davis@enron.com", "jeff.dasovich@enron.com")
    }

    def "onCreate() retrieves emails on background thread"() {
        when:
        presenter.onCreate()

        then:
        1 * apiV1.getEmailsWithLeftAndRight({ TestExecutor.executing() == backgroundExecutor }, _)
    }


    def "onCreate() should set email to view controller"() {
        when:
        presenter.onCreate()

        then:
        1 * controller.setEmails(emails);
    }

    def "onCreate() calls view controller on main thread"() {
        when:
        presenter.onCreate()

        then:
        1 * controller.setEmails({ TestExecutor.executing() == mainExecutor });
    }

}