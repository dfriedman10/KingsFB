import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

public class StudentFacebook extends PDFStreamEngine {
	
	ArrayList<Student> students;
	int currStudent;
	ArrayList<Student> subList;
	boolean clear = false, showName = false;
	int WWIDTH = 300, WHEIGHT = 580;
	int IWIDTH = WWIDTH, IHEIGHT = WHEIGHT-90;
	double correct = 0, total = 0;
	
	public StudentFacebook() {
		students = new ArrayList<Student>();
		loadFromPDF();
	    resetSubList("All Grades");
	    currStudent = (int)(Math.random()*subList.size());

		JFrame frame = new JFrame();
		JPanel outerPanel = new JPanel();
		JPanel canvas = new JPanel() {
			public void paint(Graphics g) {
				g.setFont(new Font("serif", Font.BOLD, 20));
				if (subList.size() > 0) {
					g.drawImage(subList.get(currStudent).img, 0, 0, null);
					System.out.println(subList.get(currStudent).name);
				}
				else
					g.drawString("All Students Completed!", 30, 230);
				if (showName) {
					g.setColor(Color.black);
					g.fillRect(20, 400, subList.get(currStudent).name.length()*13, 27);
					g.setColor(Color.red);
					g.drawString(subList.get(currStudent).name, 20, 420);
				}
				if (total > 0) {
					g.setColor(Color.BLACK);
					g.fillRect(240, 5, 50, 27);
					g.setColor(Color.white);
					g.drawString((int)(correct/total*100)+"%", 244, 24);
				}
				g.drawString(subList.get(currStudent).grade+"", 250, WHEIGHT-100);
			}
		};
		
		// gives our panel a certain layout to my liking - not necessary, just looks nice
		BoxLayout boxlayout = new BoxLayout(outerPanel, BoxLayout.Y_AXIS);
		outerPanel.setLayout(boxlayout);
		
		// gives our panel a nice looking border
		outerPanel.setBorder(BorderFactory.createTitledBorder("King's Facebook Quizlet"));
		
		// initializes the text container, and doesn't allow the user to type
		// into the display
		JTextArea guessBox = new JTextArea();
		guessBox.setEditable(true);
		guessBox.setPreferredSize(new Dimension(100,18));
		guessBox.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == '\n') {
					if (checkGuess(guessBox.getText().trim().toLowerCase())) {
						if (!showName) correct++;
						total++;
						next();
						frame.getContentPane().repaint();
						guessBox.requestFocus();
						showName = false;
					}
					clear = true;
				}
			}
			public void keyReleased(KeyEvent e) {}
		});
		
		JTextArea guessPrompt = new JTextArea();
		guessPrompt.setPreferredSize(new Dimension(70,18));
		guessPrompt.setText("Guess: ");
		
		JButton showButton = new JButton("Show Name");
		showButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (subList.size() > 0) {
					showName = true;
					frame.getContentPane().repaint();
					guessBox.requestFocus();
				}
			}
		});
		
		String[] choices = {"All Grades","Upper School 2019-2020", "Upper School 2018-2019","8","9","10","11","12", "Class of 2019"};
	    JComboBox<String> gradeChoice = new JComboBox<String>(choices);
	    gradeChoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String choice = (String)gradeChoice.getSelectedItem();
				resetSubList(choice);
				next();
				showName = false;
				correct = 0;
				total = 0;
				guessBox.requestFocus();
				frame.getContentPane().repaint();
			} 
	    });
	    
		JPanel buttonPanel2 = new JPanel();
		JPanel buttonPanel3 = new JPanel();
		JPanel buttonPanel4 = new JPanel();
		buttonPanel2.setPreferredSize(new Dimension(WWIDTH,30));
		buttonPanel3.setPreferredSize(new Dimension(WWIDTH,30));
		buttonPanel4.setPreferredSize(new Dimension(WWIDTH,30));
		canvas.setPreferredSize(new Dimension(IWIDTH, IHEIGHT));
		buttonPanel3.add(gradeChoice);
		buttonPanel4.add(showButton);
		guessPrompt.setBackground(buttonPanel2.getBackground());
		buttonPanel2.add(guessPrompt);
		guessBox.setText("");
		buttonPanel2.add(guessBox);
		
		outerPanel.add(buttonPanel3);
		outerPanel.add(canvas);
		outerPanel.add(buttonPanel2);
		outerPanel.add(buttonPanel4);
		
		frame.setSize(WWIDTH, WHEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		/*try {
			frame.setIconImage(ImageIO.read(getClass().getResourceAsStream("/background.png")));
		} catch (IOException e2) {
			e2.printStackTrace();
		}*/
		frame.add(outerPanel);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		guessBox.requestFocus();
		
		while (true) {
			if (clear) {
				clear = false;
				guessBox.setText("");
			}
			try {
				Thread.sleep(50);
			}catch (InterruptedException e1) {}
		}
	}
	
	protected void processOperator( Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();
        if( "Do".equals(operation) ) {
            COSName objectName = (COSName) operands.get( 0 );
            PDXObject xobject = getResources().getXObject( objectName );
            if( xobject instanceof PDImageXObject) {
                PDImageXObject image = (PDImageXObject)xobject;
 
                // same image to local
                BufferedImage bImage = new BufferedImage(image.getWidth(),image.getHeight(),
                		BufferedImage.TYPE_INT_ARGB);
                bImage = image.getImage();
                double scale = (double)IWIDTH/bImage.getWidth();
                BufferedImage scaledIm = new BufferedImage(IWIDTH, IHEIGHT, BufferedImage.TYPE_INT_ARGB);
                AffineTransform at = new AffineTransform();
                at.scale(scale,scale);
                AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                scaledIm = scaleOp.filter(bImage, scaledIm);
                
                students.add(new Student(scaledIm, null, 0));
                
            }
            else if(xobject instanceof PDFormXObject) {
                PDFormXObject form = (PDFormXObject)xobject;
                showForm(form);
            }
        }
        else {
            super.processOperator( operator, operands);
        }
    }
	
	public void processNames(String text) {
		String[] lines = text.split("\n");
		int num = 0;
		for (int i = 0; i < lines.length; i++) 
			if (lines[i].trim().equalsIgnoreCase("boarding") || lines[i].trim().equalsIgnoreCase("day")) {
				students.get(num).name = lines[i+1];
				
				try {
					String g = lines[i+2].trim();
					int grade;
					if (g.charAt(0) == '8' || g.charAt(0) == '7')
						grade = Integer.parseInt(g.substring(0,1));
					else {
						g = lines[i+3].trim();
						grade = Integer.parseInt(g.substring(0, g.indexOf("Grade")));
					}
					students.get(num).grade = grade+1;
					num++;
				}
				catch(StringIndexOutOfBoundsException e) {students.remove(num);}
			}
	}
	
	public void loadFromPDF() {
		PDDocument doc;
		try {
			doc = PDDocument.load(new File("directory.pdf"));
			for( PDPage page : doc.getPages()) {
			    processPage(page);
			}
			String text = new PDFTextStripper().getText(doc);
			processNames(text);
			if (doc != null)
				doc.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
		System.out.println("Done Loading");
	}
	
	public boolean checkGuess(String guess) {
		String currName = subList.get(currStudent).name.toLowerCase();
		while (currName.indexOf("’") != -1) 
			currName = currName.substring(0,currName.indexOf("'")) + currName.substring(currName.indexOf("'")+1);
		while (currName.indexOf("-") != -1) 
			currName = currName.substring(0,currName.indexOf("-")) +" "+ currName.substring(currName.indexOf("-")+1);
		String currFirst = currName.indexOf(" ") == -1 ? currName:currName.substring(0, currName.indexOf(" "));
		String guessFirst = guess.indexOf(" ") == -1 ? guess: guess.substring(0,guess.indexOf(" "));
		if (currFirst.equals(guessFirst) || currName.equals(guess)) {
			return true;
		}
		else if (guessFirst.length() == 4 && currFirst.length() == 4 && (""+currName.charAt(0)+currName.charAt(3)).equals("sf") &&
			(""+guess.charAt(0)+guess.charAt(3)).equals("sf"))
			return true;
		else if (guessFirst.length() == 4 && currFirst.length() == 4 && (""+currName.charAt(0)+currName.charAt(3)).equals("zd") &&
				(""+guess.charAt(0)+guess.charAt(3)).equals("zd"))
			return true;
		else if ((guessFirst.equals("hamza")||guessFirst.equals("hamzeh")) && (currFirst.equals("hamza")||
				currFirst.equals("hamzeh")))
			return true;
		else if ((guessFirst.equals("jamila")||guessFirst.equals("jamileh")) && (currFirst.equals("jamila")||
				currFirst.equals("jamileh")))
			return true;
		else if ((guessFirst.equals("sara")||guessFirst.equals("sarah")) && (currFirst.equals("sara")||
				currFirst.equals("sarah")))
			return true;
		else if ((guessFirst.equals("abdullah")||guessFirst.equals("abdallah")) && (currFirst.equals("abdullah")||
				currFirst.equals("abdallah")))
			return true;
		else if ((guessFirst.equals("ahmad")||guessFirst.equals("ahmed")) && (currFirst.equals("ahmad")||
				currFirst.equals("ahmed")))
			return true;
		else if ((guessFirst.equals("kareem")||guessFirst.equals("karim")) && (currFirst.equals("kareem")||
				currFirst.equals("karim")))
			return true;
		else if ((guessFirst.equals("nadeem")||guessFirst.equals("nadim")) && (currFirst.equals("nadeem")||
				currFirst.equals("nadim")))
			return true;
		else if ((guessFirst.equals("fares")||guessFirst.equals("faris")) && (currFirst.equals("fares")||
				currFirst.equals("faris")))
			return true;
		else if ((guessFirst.equals("mohamed")||guessFirst.equals("mohammed")||guessFirst.equals("mohammad")||guessFirst.equals("mohamad")||guessFirst.equals("muhammad")||guessFirst.equals("muhamad")) 
				&& (currFirst.equals("mohamed")||currFirst.equals("mohammed")||currFirst.equals("mohammad")||currFirst.equals("mohamad")||currFirst.equals("muhammad")||currFirst.equals("muhamad")))
			return true;
		else 
			return false;
	}
	
	public void next() {
		if (subList.size() != 0) {
			subList.remove(currStudent);
			currStudent = (int)(Math.random()*subList.size());
		}
	}
	
	public void resetSubList(String choice) {
		subList = new ArrayList<Student>();
		if (choice.equals("All Grades")) {
			for (Student s : students)
				subList.add(s);
		}
		else if (choice.equals("Class of 2019")) {
			for (Student s : students)
				if (s.grade == 13)
					subList.add(s);
		}
		else if (choice.equals("Upper School 2018-2019")) {
			for (Student s : students)
				if (s.grade <= 13 && s.grade >= 10)
					subList.add(s);
		}
		else if (choice.equals("Upper School 2019-2020")) {
			for (Student s : students)
				if (s.grade <= 12 && s.grade >= 9)
					subList.add(s);
		}
		else {
			for (Student s : students)
				if (s.grade == Integer.parseInt(choice))
					subList.add(s);
		}
		currStudent = (int)(Math.random()*subList.size());
	}
	
	public static void main(String[] args) { new StudentFacebook();}

}
