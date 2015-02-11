import org.minezy.android.ContactsActivity
import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification

class AppActivitySpecification extends RoboSpecification {
    def "should have a ListView"() {

        def contacts = new ContactsActivity(Robolectric.application)

        given:
        def contact = "me"
        expect:
        contact == "you"
    }
}