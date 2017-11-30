
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

public final class DateUtils {
    

    public final static String getAtomicTime()  {
        
        Date date= new Date();
        SimpleDateFormat sdf = new SimpleDateFormat();
sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
String yourUtcDate = sdf.format(date);
        System.out.println(yourUtcDate);
        return yourUtcDate;
    }
     private boolean check(String mail) throws IOException {
            BufferedWriter bw = null;
            boolean tru = true;
            String line1 = "";
            try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\chuny\\Desktop\\Documents\\NetBeansProjects\\ACG\\src\\javaapplication19\\password.txt"))) {
                while ((line1 = br.readLine()) != null) {
                    if (line1.equals(mail)) {
                        tru = false;
                    }
                }

            }
            return tru;
        }
    public static void main(String[] args) {
        while(true){
            System.out.println(getAtomicTime());
        }
    }
}
