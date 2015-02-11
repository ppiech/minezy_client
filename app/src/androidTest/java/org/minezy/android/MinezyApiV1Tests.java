package org.minezy.android;

import android.content.Context;
import android.test.InstrumentationTestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;

public class MinezyApiV1Tests extends InstrumentationTestCase {

    private static final String REPSONSE_CONTACTS = "{ \"contacts\": " +
                                                    "{ \"contact\": " +
                                                    "[ " +
                                                    "{ \"count\": 36616, \"email\": \"pete.davis@enron.com\", \"name\": \"Pete Davis\" }, " +
                                                    "{ \"count\": 33782, \"email\": \"vince.kaminski@enron.com\", \"name\": \"Vince J Kaminski\" }, " +
                                                    "{ \"count\": 31562, \"email\": \"jeff.dasovich@enron.com\", \"name\": \"Jeff Dasovich\" } " +
                                                    "] " +
                                                    "} " +
                                                    "}";

    private static final String REPSONSE_EMAILS = "{ \"emails\": " +
                                                  "{ \"email\": " +
                                                  "[ " +
                                                  "{ " +
                                                  "\"_ord\": 1, " +
                                                  "\"date\": { \"date\": \"Sat Jan  3 16:00:00 1970\", \"day\": 3, \"month\": 1, \"utc\": 259200.0, \"year\": 1970 }, " +
                                                  "\"id\": \"3\", \"subject\": \"My Subject\" " +
                                                  "}," +
                                                  "{ " +
                                                  "\"_ord\": 2, " +
                                                  "\"date\": { \"date\": \"Sat Jan  3 16:00:00 1970\", \"day\": 3, \"month\": 1, \"utc\": 259200.0, \"year\": 1970 }, " +
                                                  "\"id\": \"4\", \"subject\": \"My Subject\" " +
                                                  "}," +
                                                  "{ " +
                                                  "\"_ord\": 3, " +
                                                  "\"date\": { \"date\": \"Sat Jan  3 16:00:00 1970\", \"day\": 3, \"month\": 1, \"utc\": 259200.0, \"year\": 1970 }, " +
                                                  "\"id\": \"5\", \"subject\": \"My Subject\" " +
                                                  "}" +
                                                  "] " +
                                                  "} " +
                                                  "}";

    @Mock
    Context mContextMock;

    @Mock
    MinezyConnection mConnectionMock;

    MinezyApiV1 mApiV1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty(
            "dexmaker.dexcache",
            getInstrumentation().getTargetContext().getCacheDir().getPath());
        MockitoAnnotations.initMocks(this);
        mApiV1 = new MinezyApiV1(mContextMock, mConnectionMock);
    }

    public void testContacts() throws MinezyConnection.MinezyConnectionException, MinezyApiV1.MinezyApiException {
        when(mConnectionMock.requestGet("/1/100/contacts/?limit=20"))
            .thenReturn(REPSONSE_CONTACTS);

        List<Contact> expected = Arrays.asList(
            new Contact[]{new Contact("pete.davis@enron.com", "Pete Davis"),
                new Contact("vince.kaminski@enron.com", "Vince J Kaminski"),
                new Contact("jeff.dasovich@enron.com", "Jeff Dasovich")});

        assertEquals(expected, mApiV1.getContacts());
    }

    public void testEmails() throws MinezyConnection.MinezyConnectionException, MinezyApiV1.MinezyApiException {
        when(mConnectionMock
            .requestGet("/1/100/emails/?limit=20&order=asc&left=klay%40enron.com&right=savont%40email.msn.com"))
            .thenReturn(REPSONSE_EMAILS);

        Date date = new Date("Sat Jan  3 16:00:00 1970");
        List<Email> expected = Arrays.asList(
            new Email[]{new Email("3", date, "My Subject"), new Email("4", date, "My Subject"),
                new Email("5", date, "My Subject")});

        assertEquals(expected, mApiV1.getEmailsWithLeftAndRight("klay@enron.com", "savont@email.msn.com"));
    }

}
