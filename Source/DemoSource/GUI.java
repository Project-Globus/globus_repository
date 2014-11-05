/*
 * Test Bench for Project Globus Java Code
 * David Crane
 * COSC 4900 - Senior Design I
 * October 29, 2014 12:14 (GMT-7)
*/

/*
 * This class is strictly a GUI implementing the database-interaction code.
*/

import java.awt.*;
import java.awt.event.*;

import javax.swing.*; 

@SuppressWarnings("serial")
public class GUI extends JFrame implements ActionListener {
	
	JTabbedPane pane = new JTabbedPane();
	JPanel screen_Welcome = new JPanel(new SpringLayout());
	JPanel screen_Login = new JPanel(new SpringLayout());
	JPanel screen_Register = new JPanel(new SpringLayout());
	JPanel screen_Create = new JPanel(new SpringLayout());

	JButton btnLogin,  btnRegister,  btnCreate, btnSubmitL, btnSubmitR;
	JPasswordField txtPassL, txtPassR1, txtPassR2;
	JTextField txtEmailL, txtEmailR, txtName, txtBio;
	JLabel lblLogin, lblRegister, lblCreateGroup;
	
	GUI() {
	    super("Project Globus Test Bench"); setBounds(100,100,320,175);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    
	    // Welcome Screen Creation
	    {
			JLabel welcome = new JLabel("Welcome to Project Globus!");	
		    	welcome.setFont(new Font("TimesRoman", Font.BOLD, 24));
		    screen_Welcome.add(welcome);	
		    
		    btnLogin = new JButton("Login");
		    	btnLogin.addActionListener(this);
		    screen_Welcome.add(btnLogin);
		    
		    btnRegister = new JButton("Register");
		    	btnRegister.addActionListener(this);
		    screen_Welcome.add(btnRegister);
		    
		    SpringUtilities.makeCompactGrid(screen_Welcome, 3, 1, 6, 6, 6, 6);
	    }
	    
	    // Login Screen Creation
	    {
	    	JLabel lblEmail = new JLabel("E-Mail:");	
	    	screen_Login.add(lblEmail);
	    	txtEmailL = new JTextField(20);
	    	screen_Login.add(txtEmailL);
	    	
	    	JLabel lblPassword = new JLabel("Password:");	
	    	screen_Login.add(lblPassword);
	    	txtPassL = new JPasswordField(20);
	    	screen_Login.add(txtPassL);
	    	
	    	btnSubmitL = new JButton("Login");
	    		btnSubmitL.addActionListener(this);
	    	screen_Login.add(btnSubmitL);
	    	
	    	lblLogin = new JLabel();
	    	screen_Login.add(lblLogin);
	    	
	    	SpringUtilities.makeCompactGrid(screen_Login, 3, 2, 6, 6, 6, 6);
	    	
	    }
	    
	 // Register Screen Creation
	    {
	    	JLabel lblName = new JLabel("Your Name:");
	    	screen_Register.add(lblName);
	    	txtName = new JTextField(20);
	    	screen_Register.add(txtName);
	    	
	    	JLabel lblBio = new JLabel("Biography:");	
	    	screen_Register.add(lblBio);
	    	txtBio = new JTextField(200);
	    	screen_Register.add(txtBio);
	    	
	    	JLabel lblEmail = new JLabel("E-Mail:");	
	    	screen_Register.add(lblEmail);
	    	txtEmailR = new JTextField(20);
	    	screen_Register.add(txtEmailR);

	    	JLabel lblPassword = new JLabel("Password:");	
	    	screen_Register.add(lblPassword);
	    	txtPassR1 = new JPasswordField(20);
	    	screen_Register.add(txtPassR1);
	    	
	    	JLabel lblPassword2 = new JLabel("Re-enter Password:");	
	    	screen_Register.add(lblPassword2);
	    	txtPassR2 = new JPasswordField(20);
	    	screen_Register.add(txtPassR2);
	    	
	    	btnSubmitR = new JButton("Register");
	    		btnSubmitR.addActionListener(this);
	    	screen_Register.add(btnSubmitR);
	    	
	    	lblRegister = new JLabel();
	    	screen_Register.add(lblRegister);
	    	
	    	SpringUtilities.makeCompactGrid(screen_Register, 6, 2, 6, 6, 6, 6);
	    	
	    }
	    
	    btnCreate = new JButton("Create Group");
	    
	    
	    
	    pane.addTab("Welcome", screen_Welcome);
	    pane.addTab("Login", screen_Login);
	    pane.addTab("Register", screen_Register);
	    pane.addTab("Create", screen_Create);
	    
	    Container base = this.getContentPane(); 
	    base.add(pane); 
	    
	    setVisible(true);
	    
	  }
	@SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnLogin) {
			pane.setSelectedIndex(1);
			super.setSize(320,175);
		} else if (e.getSource() == btnRegister) {
			pane.setSelectedIndex(2);
			super.setSize(320,260);
		} else if (e.getSource() == btnSubmitL) {
			// Send Login
			if (!PGDB.getInit()) PGDB.init();
			else {
				lblLogin.setText(PGDB.login(txtEmailL.getText(), txtPassL.getText()));
				txtPassL.setText("");
			}
		} else if (e.getSource() == btnSubmitR) {
			// Send Register
			if (!PGDB.getInit()) PGDB.init();
			else {
				if (txtPassR1.getPassword().equals(txtPassR2.getPassword())) {
					lblRegister.setText("Passwords do not match!");
					return;
				} else if (txtPassR2.getPassword().length < 6) {
					lblRegister.setText("Password too short. Minimum 6 characters.");
					return;
				} else if (txtName.getText().length() < 2) {
					lblRegister.setText("Name too short. Minimum 1 character.");
					return;
				} else if (txtEmailR.getText().length() < 7) {
					lblRegister.setText("Invalid email.");
					return;
				} else {
					lblRegister.setText(PGDB.createAccount(txtName.getText(), 
						txtBio.getText(), txtEmailR.getText(), txtPassR2.getText()));
					
					txtName.setText("");
					txtEmailR.setText("");
					txtBio.setText("");
					txtPassR1.setText("");
					txtPassR2.setText("");
				}
			}
		}
	}
}