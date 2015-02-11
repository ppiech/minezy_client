import android.widget.TextView
import org.minezy.android.ContactsActivity
import org.robolectric.Robolectric

def "should display hello text"() {
    given:
    def textView = new TextView(Robolectric.application)

    def contactsActiviy = new ContactsActivity(Robolectric.application)

    and:
    def hello = "Hello"

    when:
    textView.setText(hello)

    then:
    textView.getText() == "yo"
}