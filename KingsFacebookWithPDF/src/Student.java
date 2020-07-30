import java.awt.image.BufferedImage;

public class Student {
	int grade = 0; String name = "Name not found";
	BufferedImage img;
	
	public Student(BufferedImage i, String name, int g) {
		img = i; this.name = name; grade = g;
	}
}
