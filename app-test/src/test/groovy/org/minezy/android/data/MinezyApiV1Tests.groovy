package org.minezy.android.data

import android.content.Context
import org.minezy.android.model.Contact
import org.minezy.android.model.Email
import pl.polidea.robospock.RoboSpecification

class MinezyV1ApiSpecification extends RoboSpecification {

    def context = Mock(Context)
    def connection = Mock(MinezyConnection);
    def apiV1 = new MinezyApiV1(context, connection);

    def "getContacts() should return list of contacts"() {

        when:
        def contactsList =
                [
                        new Contact("pete.davis@enron.com", "Pete Davis"),
                        new Contact("vince.kaminski@enron.com", "Vince J Kaminski"),
                        new Contact("jeff.dasovich@enron.com", "Jeff Dasovich")
                ]

        connection.requestGet("/1/100/contacts/?limit=20") >> """\
            {
                "contacts": {
                    "_query": "MATCH (n:`100`:Contact) WITH n,n.sent+n.to+n.cc+n.bcc AS count WHERE count > 0 RETURN COALESCE(n.name,n.email) as name,n.email,count ORDER BY count DESC, name ASC LIMIT {limit}",
                    "_query_time": 5.302954196929932,
                    "contact": [
                            {
                                "count": 36616,
                                "email": "pete.davis@enron.com",
                                "name": "Pete Davis"
                            },
                            {
                                "count": 33782,
                                "email": "vince.kaminski@enron.com",
                                "name": "Vince J Kaminski"
                            },
                            {
                                "count": 31562,
                                "email": "jeff.dasovich@enron.com",
                                "name": "Jeff Dasovich"
                            }
                    ]
                }
            }
            """

        then:
        apiV1.getContacts() == contactsList
    }

    def "getEmails() should return list of emails"() {
        when:
        connection.requestGet("/1/100/emails/?limit=20&order=asc&left=klay%40enron.com&right=savont%40email.msn.com") >> """\
                {
                  "emails": {
                    "_count": 20,
                    "_query": "MATCH (cL:`100`:Contact)-[rL:SENT|TO|CC|BCC]-(e:`100`:Email)-[rR:SENT|TO|CC|BCC]-(cR:`100`:Contact) WHERE cL.email IN {left} AND cR.email IN {right} AND (type(rL)='SENT' OR type(rR)='SENT') RETURN distinct(e) ORDER BY e.timestamp ASC LIMIT {limit}",
                    "_query_time": 0.230194091796875,
                    "email": [
                      {
                        "_ord": 1,
                        "date": {
                          "date": "Wed, 13 Sep 2000 14:42:00 -0700 (PDT)",
                          "day": 13,
                          "month": 9,
                          "utc": 968881320,
                          "year": 2000
                        },
                        "id": "5964974.1075840259917.JavaMail.evans@thyme",
                        "subject": "Talking points from Dave Walden"
                      },
                      {
                        "_ord": 2,
                        "date": {
                          "date": "Wed, 13 Sep 2000 14:42:00 -0700 (PDT)",
                          "day": 13,
                          "month": 9,
                          "utc": 968881320,
                          "year": 2000
                        },
                        "id": "13123665.1075840236503.JavaMail.evans@thyme",
                        "subject": "Talking points from Dave Walden"
                      },
                      {
                        "_ord": 3,
                        "date": {
                          "date": "Wed, 13 Sep 2000 14:42:00 -0700 (PDT)",
                          "day": 13,
                          "month": 9,
                          "utc": 968881320,
                          "year": 2000
                        },
                        "id": "32558075.1075840209050.JavaMail.evans@thyme",
                        "subject": "Talking points from Dave Walden"
                      }
                   ]
                  }
                }
        """
        def emailList =
                [
                        new Email("5964974.1075840259917.JavaMail.evans@thyme", new Date("Wed, 13 Sep 2000 14:42:00 -0700 (PDT)"), "Talking points from Dave Walden"),
                        new Email("13123665.1075840236503.JavaMail.evans@thyme", new Date("Wed, 13 Sep 2000 14:42:00 -0700 (PDT)"), "Talking points from Dave Walden"),
                        new Email("32558075.1075840209050.JavaMail.evans@thyme", new Date("Wed, 13 Sep 2000 14:42:00 -0700 (PDT)"), "Talking points from Dave Walden")
                ]



        then:
        apiV1.getEmailsWithLeftAndRight("klay@enron.com", "savont@email.msn.com") == emailList
    }


}