package admin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controller for admin UI navigation and actions.
 * Keeps UI logic separate from components.
 */
public class AdminController {
    private final AdminFrame frame;

    public AdminController(AdminFrame frame) {
        this.frame = frame;
    }

    public ActionListener navigate(final String cardName) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.showCard(cardName);
            }
        };
    }

    public ActionListener logoutAction() {
        return e -> {
            // perform logout and close the admin frame
            admin.service.AdminAuth.getInstance().logout();
            frame.dispose();
        };
    }
}
