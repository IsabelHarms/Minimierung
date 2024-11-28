import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class PanelMinimizing extends JPanel {
    public PanelMinimizing() {
        setLayout(new BorderLayout());

        JButton backButton = new JButton("← Back");
        JButton nextButton = new JButton("Next →");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);

        add(buttonPanel, BorderLayout.SOUTH);

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // For now, do nothing
                System.out.println("Back button clicked");
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // For now, do nothing
                System.out.println("Next button clicked");
            }
        });
    }
}
