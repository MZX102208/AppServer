/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cfappserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import org.apache.commons.lang3.StringEscapeUtils;

public class Bot {

    static String startAssignTable = "<td>Date Due</td><td>Date Assigned</td><td>Assignment</td><td>Category</td><td>Score</td><td>Total Points</td><td class=\"sg-view-quick\">Weight</td><td class=\"sg-view-quick\">Weighted Score</td><td class=\"sg-view-quick\">Weighted Total Points</td><td class=\"sg-view-quick\">Percentage</td>";
    static String startAssignWeight = "<td align=\"center\" valign=\"middle\">Category</td><td align=\"center\" valign=\"middle\">Student's<br>Points</td><td align=\"center\" valign=\"middle\"> / Maximum<br>Points</td><td align=\"center\" valign=\"middle\"> = Percent</td><td align=\"center\" valign=\"middle\"> * Category<br>Weight</td><td align=\"center\" valign=\"middle\"> = Category<br>Points</td>";

    public static String genData(String user, String pass, int semnum) {
        try {
            String url = "https://home-access.cfisd.net/HomeAccess/Account/LogOn?ReturnUrl=%2fhomeaccess%2f";
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setSoTimeout(httpParams, 10000);
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpClient client = new DefaultHttpClient(httpParams);
            CookieStore httpCookieStore = new BasicCookieStore();
            HttpClientBuilder builder = HttpClientBuilder.create().setDefaultCookieStore(httpCookieStore);
            HttpPost post = new HttpPost(url);
            Scanner kk = new Scanner(System.in);
            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("Database", "10"));
            urlParameters.add(new BasicNameValuePair("LogOnDetails.UserName", user));
            urlParameters.add(new BasicNameValuePair("LogOnDetails.Password", pass));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
            }

            response = client.execute(new HttpGet("https://home-access.cfisd.net/HomeAccess/Classes/Classwork"));
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            result = new StringBuffer();
            line = "";
            ClssPkg classpack = null;
            while ((line = rd.readLine()) != null) {
                if (line.contains("<li class=\"sg-banner-menu-element sg-menu-element-identity\">")) {
                    classpack = new ClssPkg(rd.readLine().split("<|>")[2],semnum);
                }
            }
            if (classpack == null) {
                return "Wrong login";
            }
            getReport(client, classpack);
            getLunch(client, classpack);
            getAssignments(client, classpack);
            getAbsences(client, classpack, urlParameters);
            return classpack.toString();
        } catch (Exception ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Wrong login";
    }

    public static void getAssignments(HttpClient client, ClssPkg cp) {
        try {
            HttpResponse response = client.execute(new HttpGet("https://home-access.cfisd.net/HomeAccess/Content/Student/Assignments.aspx"));

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = "";
            Course c = null;
            while ((line = rd.readLine()) != null) {
                if (line.contains("a class=\"sg-header-heading\"")) {
                    String s = remSpace(rd.readLine());
                    c = cp.get(s);
                    if(c==null){
                        c = cp.get(s+" P/F");
                    }
                }
                if (line.contains(startAssignTable) && c!=null) {
                    while ((line = rd.readLine()).contains("</tr><tr class=\"sg-asp-table-data-row\">")) {
                        String duedate = remSpace(rd.readLine().split("<|>")[2]);
                        rd.readLine();
                        String name = remSpace(rd.readLine());
                        rd.readLine();
                        rd.readLine();
                        String s = rd.readLine();
                        while(s.split("<|>").length < 12)s+=" "+rd.readLine();
                        s = removeExtra(s);
                        s = s.replaceAll("<strike>\\d*\\.\\d*\\s*<\\/strike>", "X");
                        String[] arr = s.split("<|>");
                        String type = remSpace(arr[4]);
                        arr[8] = remSpace(arr[8].toUpperCase());
                        String grade = "0.0";
                        if (arr[8].contains("Z")) {
                            grade = "" + 0.0;
                        } else {
                            try {
                                double grad = Double.parseDouble(arr[8]);
                                grade = "" + grad;
                            } catch (Exception e) {
                                grade = "X";
                            }
                        }
                        String comment = "";
                        String totalscore = "";
                        if(s.contains("class=\"comment-tooltip\"")){
                            comment = arr[9].substring(arr[9].indexOf("title=\"")+7,arr[9].length()-1);
                            totalscore = remSpace(arr[14]);
                        } else {
                            totalscore = remSpace(arr[12]);
                        }
                        Assignment a = new Assignment(duedate, name, type, grade, totalscore, comment);
                        c.addAssign(a);
                    }
                }
                if (line.contains(startAssignWeight) && c!=null) {
                    while ((line = rd.readLine()).contains("</tr><tr class=\"sg-asp-table-data-row\">")) {
                        String[] arr = rd.readLine().split("<|>");
                        String type = remSpace(arr[2]);
                        double weight = Double.parseDouble(remSpace(arr[18]));
                        double wscore = Double.parseDouble(remSpace(arr[22]));
                        Type t = new Type(type, weight, wscore);
                        c.addType(t);
                    }
                }
            }
        } catch (IOException ex) {

        }

    }

    public static void getAbsences(HttpClient client, ClssPkg cp, List<NameValuePair> urlParameters) {
        String numabs = "";
        String month = "";
        String url = "https://home-access.cfisd.net/HomeAccess/Content/Attendance/MonthlyView.aspx";
        BasicNameValuePair bnvp = null;
        BasicNameValuePair bnvp2 = null;
        BasicNameValuePair bnvp3 = null;
        BasicNameValuePair bnvp4 = null;
        TreeMap<String, Integer> absen = new TreeMap<String, Integer>();
        while (!month.equals("January") && !month.equals("August")) {
            try {
                HttpPost post = new HttpPost(url);
                urlParameters = new ArrayList<NameValuePair>();
                if (bnvp != null && bnvp2 != null && bnvp3 != null && bnvp4 != null) {
                    urlParameters.add(bnvp);
                    urlParameters.add(bnvp2);
                    urlParameters.add(bnvp3);
                    urlParameters.add(bnvp4);
                }
                post.setEntity(new UrlEncodedFormEntity(urlParameters));
                HttpResponse response = client.execute(post);

                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    String total = "";
                    if (line.contains("Monday")) {
                        total = line.replace("\n", "").replace("\r", "");
                        while (!(line = rd.readLine()).equals("</table>")) {
                            total += "-" + line.replace("\n", "").replace("\r", "");
                        }
                        String[] arr = total.split("\"");
                        ArrayList<String> a = new ArrayList<String>();
                        for (int i = 0; i < arr.length; i++) {
                            if (arr[i].contains("td title=")) {
                                String[] arr2 = arr[i + 1].split("-");
                                for (String s : arr2) {
                                    if (!s.contains("AM") && !s.contains("PM")) {
                                        a.add(s);
                                    }
                                }
                            }
                        }
                        for (int i = 0; i < a.size(); i++) {
                            if (unExcused(a.get(i + 1))) {
                                addToTree(a.get(i), absen);
                            }
                            if (a.get(i + 1).equals("Tardy")) {
                                addToTree("T", absen);
                            }
                            i++;
                        }

                    }
                    if (line.contains("__EVENTVALIDATION")) {
                        bnvp4 = new BasicNameValuePair("__EVENTVALIDATION", line.split("\"")[7]);
                    }
                    if (line.contains("__VIEWSTATE")) {
                        bnvp3 = new BasicNameValuePair("__VIEWSTATE", line.split("\"")[7]);
                    }
                    if (line.contains("Go to the previous month")) {
                        String[] arr = line.split("'");
                        bnvp = new BasicNameValuePair("__EVENTTARGET", arr[1]);
                        bnvp2 = new BasicNameValuePair("__EVENTARGUMENT", arr[3]);
                        month = getMonth(line);
                    }
                }
            } catch (Exception ex) {

            }
        }
        for (Map.Entry<String, Integer> e : absen.entrySet()) {
            numabs += e.getKey() + "-" + e.getValue() + " ";
        }
        cp.addAbsences(numabs);
    }

    public static void getReport(HttpClient client, ClssPkg cp) {
        try {
            HttpResponse response = client.execute(new HttpGet("https://home-access.cfisd.net/HomeAccess/Content/Student/ReportCards.aspx"));

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                if (line.contains("</tr><tr class=\"sg-asp-table-data-row\">")) {
                    String cnum = remSpace(rd.readLine().split("<|>")[2]);
                    String cname = remSpace(rd.readLine().split("<|>")[2]);
                    String[] arr = rd.readLine().split("<|>|\"|:");
                    int pnum = Integer.parseInt(remSpace(arr[6]));
                    String email = remSpace(arr[13]);
                    String teacher = remSpace(arr[15]);
                    String roomnum = remSpace(arr[21]);
                    rd.readLine();
                    rd.readLine();
                    rd.readLine();
                    rd.readLine();
                    rd.readLine();
                    String mp1 = remSpace(rd.readLine().split("<|>")[2]);
                    rd.readLine();
                    rd.readLine();
                    rd.readLine();
                    String mp2 = remSpace(rd.readLine().split("<|>")[2]);
                    rd.readLine();
                    rd.readLine();
                    rd.readLine();
                    String mp3 = remSpace(rd.readLine().split("<|>")[2]);
                    rd.readLine();
                    rd.readLine();
                    rd.readLine();
                    String f1 = remSpace(rd.readLine().split("<|>")[2]);
                    rd.readLine();
                    rd.readLine();
                    rd.readLine();
                    String sem1 = remSpace(rd.readLine().split("<|>")[2]);
                    rd.readLine();
                    rd.readLine();
                    rd.readLine();
                    String mp4 = remSpace(rd.readLine().split("<|>")[2]);
                    rd.readLine();
                    rd.readLine();
                    rd.readLine();
                    String mp5 = remSpace(rd.readLine().split("<|>")[2]);
                    rd.readLine();
                    rd.readLine();
                    rd.readLine();
                    String mp6 = remSpace(rd.readLine().split("<|>")[2]);
                    rd.readLine();
                    rd.readLine();
                    rd.readLine();
                    String f2 = remSpace(rd.readLine().split("<|>")[2]);
                    rd.readLine();
                    rd.readLine();
                    rd.readLine();
                    String sem2 = remSpace(rd.readLine().split("<|>")[2]);
                    Course c = new Course(cname, teacher, email, roomnum, cnum, pnum, mp1, mp2, mp3, f1, sem1, mp4, mp5, mp6, f2, sem2);
                    cp.classes.add(c);
                }
            }

        } catch (IOException ex) {

        }

    }
    
    public static void getLunch(HttpClient client, ClssPkg cp) {
        try {
            HttpResponse response = client.execute(new HttpGet("https://home-access.cfisd.net/HomeAccess/Content/Student/Classes.aspx"));
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = "";
            String count = "";
            ArrayList<Period> periods = new ArrayList<Period>();
            periods.add(new Period("1"));
            periods.add(new Period("2"));
            periods.add(new Period("3"));
            periods.add(new Period("41"));
            periods.add(new Period("42"));
            periods.add(new Period("45"));
            periods.add(new Period("52"));
            periods.add(new Period("53"));
            periods.add(new Period("6"));
            periods.add(new Period("7"));
            while ((line = rd.readLine()) != null) {
                if(line.contains("</tr><tr class=\"sg-asp-table-data-row\">")){
                    String[] arr = rd.readLine().split("<|>");
                    String classnum = remSpace(arr[2]);
                    arr = rd.readLine().split("<|>");
                    String classname = remSpace(arr[2]);
                    CourseWrapper c = new CourseWrapper(classnum, classname);
                    arr = rd.readLine().split("<|>");
                    String periodnum = remSpace(arr[4]);
                    String email = "";
                    String teacher = "";
                    String roomnum = "";
                    if(arr.length>=34){
                        email = remSpace(arr[9].substring(arr[9].indexOf(":")+1,arr[9].length()-1));
                        teacher = remSpace(arr[10]);
                        roomnum = remSpace(arr[16]);
                    } else {
                        teacher = remSpace(arr[8]);
                        roomnum = remSpace(arr[12]);
                    }
                    if(cp.get(classnum+" "+classname)==null && !classname.toLowerCase().equals("lunch")){
                        cp.classes.add(new Course(classname,teacher,email,roomnum,classnum,Integer.parseInt(periodnum),"","","","","","","","","",""));
                    }
                    for(Period  p : periods){
                        if(p.periodnum.equals(periodnum)){
                            if(p.semester1==null){
                                p.semester1 = c;
                            } else if(p.semester2==null){
                                p.semester2 = c;
                            }
                        }
                    }
                }
            }
            
            ArrayList<String> sem1arr = new ArrayList<String>();
            ArrayList<String> sem2arr = new ArrayList<String>();
            ArrayList<String> cn1arr = new ArrayList<String>();
            ArrayList<String> cn2arr = new ArrayList<String>();
            
            for(Period p : periods){
                if(sem1arr.size()>=2) {
                    if(!p.semester1.name.equals(sem1arr.get(sem1arr.size()-1)) && sem1arr.get(sem1arr.size()-2).equals(sem1arr.get(sem1arr.size()-1))){
                        sem1arr.remove(sem1arr.size()-1);
                        cn1arr.remove(cn1arr.size()-1);
                    }
                    sem1arr.add(p.semester1.name);
                    cn1arr.add(p.semester1.coursenum);
                } else {
                    sem1arr.add(p.semester1.name);
                    cn1arr.add(p.semester1.coursenum);
                }
                if(sem2arr.size()>=2) {
                    if(!p.semester2.name.equals(sem2arr.get(sem2arr.size()-1)) && sem2arr.get(sem2arr.size()-2).equals(sem2arr.get(sem2arr.size()-1))){
                        sem2arr.remove(sem2arr.size()-1);
                        cn2arr.remove(cn2arr.size()-1);
                    }
                    sem2arr.add(p.semester2.name);
                    cn2arr.add(p.semester2.coursenum);
                } else {
                    sem2arr.add(p.semester2.name);
                    cn2arr.add(p.semester2.coursenum);
                }
            }
            
            
            for(int i=0;i<sem1arr.size();i++){
                cp.sem1cnum.add(cn1arr.get(i));
                cp.schedulesem1.add(sem1arr.get(i));
            }
            for(int i=0;i<sem2arr.size();i++){
                cp.sem2cnum.add(cn2arr.get(i));
                cp.schedulesem2.add(sem2arr.get(i));
            }
        } catch (IOException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static String remSpace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if ((int) s.charAt(i) > 32) {
                s = s.substring(i);
                break;
            }
        }
        for (int i = s.length() - 1; i >= 0; i--) {
            if ((int) s.charAt(i) > 32) {
                s = s.substring(0, i + 1);
                break;
            }
        }
        for (int i = 1; i < s.length(); i++) {
            if ((int) s.charAt(i) <= 32 && (int) s.charAt(i - 1) <= 32) {
                s = s.substring(0, i - 1) + s.substring(i);
                i--;
            }
        }
        s = StringEscapeUtils.unescapeHtml4(s);
        s = s.replace("{", "(");
        s = s.replace("}", ")");
        return s;
    }
    public static boolean unExcused(String s){
        s = s.replace("&#39;","'");
        String[] names = {"After Tardy","DMC Absences","Doctor's Note","Excused","Excused Parental Consent","Excused Runaway","Funding Only Absence",
            "Home Clinic","Late Note","Left Message"," Legal","No Contact","Parent Contact","Runaway Unexcused","Skipping","Student Contact",
            "Testing Absence","Truant","Unexcused"};
        for(String n : names){
            if(s.equals(n))return true;
        }
        return false;
    }
    public boolean has(String s, ArrayList<String> al) {
        for (int i = 0; i < al.size(); i++) {
            if (al.get(i).equals(s)) {
                return true;
            }
        }
        return false;
    }
    public static TreeMap<String, Integer> addToTree(String a, TreeMap<String, Integer> tm){
        if(tm.containsKey(a))tm.put(a, 1+tm.get(a));
        else tm.put(a, 1);
        return tm;
    }
    public static String getMonth(String s){
        String[] months = {"January", "February","March","April","May","June","July","August","September","October","November","December"};
        String month = "";
        for(String str : months){
            if(s.contains(str))month = s.substring(s.indexOf(str),s.indexOf(str)+str.length());
        }
        return month;
    }
    
    public static String removeExtra(String s) {
        boolean open = false;
        s = s.replace("->","to");
        s = s.replace("=>","to");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            String tmp = s;
            if (c == '<' && open) {
                s = removeChar(s, i);
                i--;
            } else if (c == '>' && !open) {
                s = removeChar(s, i);
                i--;
            } else if (c == '<' || c == '>') {
                open = !open;
            }
        }
        if(s.charAt(s.length()-1)=='<')s = s.substring(0, s.length()-1);
        return s;
    }

    public static String removeChar(String s, int i) {
        if (i == (s.length() - 1)) {
            s = s.substring(0, i);
        } else {
            s = s.substring(0, i) + s.substring(i + 1);
        }
        return s;
    }
}

class Period implements Comparable{
    CourseWrapper semester1 = null;
    CourseWrapper semester2 = null;
    String periodnum;
    public Period(String p){
        periodnum = p;
    }

    @Override
    public int compareTo(Object o) {
        return periodnum.compareTo(((Period)o).periodnum);
    }
}
class CourseWrapper { 
    String coursenum;
    String name;
    public CourseWrapper(String c, String n){
        coursenum = c;
        name = n;
    }
    
    public String toString(){
        return name;
    }
}
