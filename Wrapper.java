package neuralnet2;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Wrapper extends JFrame {

	public static final int FRAMESIZE = 800;
	public final int HRZSPACE = 8;
	
	public Wrapper() {
		ControllerMS controller = new ControllerMS(Params.WIN_WIDTH,Params.WIN_HEIGHT);
        setSize(2*FRAMESIZE, 3*FRAMESIZE);
		add(controller);
        setResizable(false);
        setTitle("Dot Collector");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                controller.save();
                System.exit(0);
            }
        });
	}
	
	
	public static void main(String[] args) {
		
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Wrapper go = new Wrapper();
                go.setVisible(true);
            }
        });
    
        
	}
	
}
