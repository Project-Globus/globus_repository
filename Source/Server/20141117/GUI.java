

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
	JPanel screen_Join = new JPanel(new SpringLayout());

	JButton btnLogin,  btnRegister,  btnCreate, btnSubmitL, btnSubmitR;
	JPasswordField txtPassL, txtPassR1, txtPassR2, txtGroupPassC1, txtGroupPassC2, txtGroupPassJ, txtGroupGooglePass;
	JTextField txtEmailL, txtEmailR, txtName, txtBio, txtGroupName, txtGroupDesc, txtGroupGoogleUser;
	JLabel lblLogin, lblRegister, lblCreate, lblJoin;
	
	String[] userInfo = new String[7];
	String[] groupInfo = new String[8];
	
	GUI() {
	    super("Project Globus Test Bench"); setBounds(100,100,420,175);
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
	    // Create Group Screen
	    {
	    	JLabel lblGName = new JLabel("Group Name:");
	    	screen_Create.add(lblGName);
	    	txtGroupName = new JTextField(20);
	    	screen_Create.add(txtGroupName);
	    	
	    	JLabel lblGDesc = new JLabel("Group Description:");
	    	screen_Create.add(lblGDesc);
	    	txtGroupDesc = new JTextField(200);
	    	screen_Create.add(txtGroupDesc);
	    	
	    	JLabel lblGPass = new JLabel("Group Password:");
	    	screen_Create.add(lblGPass);
	    	txtGroupPassC1= new JPasswordField(20);
	    	screen_Create.add(txtGroupPassC1);
	    	
	    	JLabel lblGPass2 = new JLabel("Re-enter Password:");
	    	screen_Create.add(lblGPass2);
	    	txtGroupPassC2= new JPasswordField(20);
	    	screen_Create.add(txtGroupPassC2);
	    		    	
	    	btnCreate = new JButton("Create Group");
	    		btnCreate.addActionListener(this);
	    	screen_Create.add(btnCreate);
	    	
	    	lblCreate = new JLabel();
	    	screen_Create.add(lblCreate);
	    	
	    	SpringUtilities.makeCompactGrid(screen_Create, 5, 2, 6, 6, 6, 6);
	    }
	    
	    
	    
	    
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
			super.setSize(420,175);
		} else if (e.getSource() == btnRegister) {
			pane.setSelectedIndex(2);
			super.setSize(420,260);
		} else if (e.getSource() == btnSubmitL) {
			// Send Login
			if (!PGDB.getInit()) PGDB.init();
			
			userInfo = PGDB.login(txtEmailL.getText(), txtPassL.getText());
			
			lblLogin.setText(userInfo[0]);
			txtPassL.setText("");
		} else if (e.getSource() == btnSubmitR) {
			// Send Register
			if (!PGDB.getInit()) PGDB.init();
			if (!txtPassR1.getText().equals(txtPassR2.getText())) {
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
				userInfo = PGDB.createAccount(txtName.getText(), 
						txtBio.getText(), txtEmailR.getText(), txtPassR2.getText());
				
				lblRegister.setText(userInfo[0]);
				
				txtName.setText("");
				txtEmailR.setText("");
				txtBio.setText("");
				txtPassR1.setText("");
				txtPassR2.setText("");
			}
		} else if (e.getSource() == btnCreate) {
			// Send Create
			if (!PGDB.getInit()) PGDB.init();
			
			if (!txtGroupPassC1.getText().equals(txtGroupPassC2.getText())) {
				lblCreate.setText("Passwords do not match!");
				return;
			} else if (txtGroupPassC2.getPassword().length < 6) {
				lblCreate.setText("Password too short. Minimum 6 characters.");
				return;
			} else if (txtGroupName.getText().length() < 2) {
				lblCreate.setText("Name too short. Minimum 1 character.");
				return;
			} else if (userInfo[1] == "" || userInfo[1] == null) {
				lblCreate.setText("Please sign in.");
				return;
			} else {
				groupInfo = PGDB.createGroup(userInfo, 
						txtGroupName.getText(), txtGroupDesc.getText(), txtGroupPassC2.getText());
				
				lblCreate.setText(groupInfo[0]);
				
				txtGroupName.setText("");
				txtGroupDesc.setText("");
				txtGroupPassC1.setText("");
				txtGroupPassC2.setText("");
			}
		
		}
	}
}