//Prajwal Prasad
//1001750483

//https://github.com/RegisDeVallis/Java_Log_in_JTextArea/blob/master/TextAreaOutputStream.java- Convert sysout messages and write on JtextArea
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JTextArea;

//This class is used to write the standart console output to Jtextarea
public class TextAreaOutputStream extends OutputStream {  
    private static final TextAreaOutputStream INSTANCE = new TextAreaOutputStream();  
    private static final PrintStream OUT;  
    private static JTextArea outWriter;  
    private static boolean TAINTED = false;  
    static {  
        OUT = System.out;  
        System.setOut(new PrintStream(new TextAreaOutputStream()));  
    }  
   
    /** Creates a new instance of TextAreaOutputStream. */  
    private TextAreaOutputStream() {}  
   
    /** Gets the output stream. */  
    public static TextAreaOutputStream getInstance(JTextArea textArea) {  
        outWriter = textArea;  
        return INSTANCE;  
    }  
   
    /** Gets the functioning console output. 
      * @see java.lang.System.out 
      */  
    public static PrintStream getOldSystemOut() {  
        return OUT;  
    }  
   
    /** Determines if output has occured. */  
    public static boolean isTainted() {  
        return TAINTED;  
    }  
   
    /** Write output to the Text Area. */  
    public void write(int param) {  
        outWriter.setText(outWriter.getText() + (char)param);  
        TAINTED = true;  
    }  
}  