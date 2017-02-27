/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cfappserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author User1
 */
public class ClssPkg {

    String lunch;
    int[] abs = new int[8];
    String absString = "";
    String name;
    ArrayList<Course> classes = new ArrayList<Course>();
    ArrayList<String> schedulesem1 = new ArrayList<String>();
    ArrayList<String> schedulesem2 = new ArrayList<String>();
    ArrayList<String> sem1cnum = new ArrayList<String>();
    ArrayList<String> sem2cnum = new ArrayList<String>();
    int semval = 1;

    public ClssPkg(String n, int sem) {
        name = n;
        semval = sem;
    }

    public Course get(String n) {
        for (Course c : classes) {
            if ((c.coursenum + " " + c.name).equals(n)) {
                return c;
            }
        }
        return null;
    }

    public void addAssign(String n, Assignment a) {
        for (Course c : classes) {
            if (c.name.equals(n)) {
                c.addAssign(a);
            }
        }
    }

    public void addType(String n, Type a) {
        for (Course c : classes) {
            if (c.name.equals(n)) {
                c.addType(a);
            }
        }
    }

    public void addAbsences(String a) {
        absString = a;
        String[] arr = a.split(" ");
        int subtract1 = 0;
        for (int i = 0; i < arr.length; i++) {
            String[] tmp = arr[i].split("-");
            if (tmp.length >= 2) {
                int numabs = Integer.parseInt(tmp[1]);
                if (!tmp[0].equals("T")) {
                    int periodnum = Integer.parseInt(tmp[0]);
                    for (Course c : classes) {
                        if (c.periodnum == periodnum) {
                            if(semval==1){
                                for (int j = 0; j < sem1cnum.size(); j++) {
                                    if (schedulesem1.get(i).equals("Lunch")) {
                                        subtract1 = 1;
                                    }
                                    if (sem1cnum.get(j).equals(c.coursenum) && abs[j] == 0) {
                                        abs[j - subtract1] = numabs;
                                    }
                                }
                            } else {
                                for (int j = 0; j < sem2cnum.size(); j++) {
                                    if (schedulesem2.get(i).equals("Lunch")) {
                                        subtract1 = 1;
                                    }
                                    if (sem2cnum.get(j).equals(c.coursenum) && abs[j] == 0) {
                                        abs[j - subtract1] = numabs;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    abs[7] = numabs;
                    abs[7] = numabs;
                }
            }
        }
    }

    public String toString() {
        String ret = "{" + name + "}";
        ret += classes.size();
        for (Course c : classes) {
            ret += c.toString();
        }
        for (int i = 0; i < abs.length; i++) {
            ret += "{" + abs[i] + "}";
        }
        ret += schedulesem1.size();
        for (String s : schedulesem1) {
            ret += "{" + s + "}";
        }
        ret += "{" + schedulesem2.size();
        for (String s : schedulesem2) {
            ret += "{" + s + "}";
        }
        return ret;
    }

    public static ClssPkg parse(String str, int sem) {
        Scanner s = new Scanner(str);
        s.useDelimiter("\\}\\{|\\}|\\{");
        ClssPkg clss = new ClssPkg(s.next(),sem);
        int iter1 = s.nextInt();
        for (int i = 0; i < iter1; i++) {
            Course c = new Course(s.next(), s.next(), s.next(), s.next(), s.next(), s.nextInt(), s.next(), s.next(), s.next(), s.next(), s.next(), s.next(), s.next(), s.next(), s.next(), s.next());
            int iter2 = s.nextInt();
            for (int j = 0; j < iter2; j++) {
                Assignment a = new Assignment(s.next(), s.next(), s.next(), s.next(), s.next(), s.next());
                c.addAssign(a);
            }
            iter2 = s.nextInt();
            for (int j = 0; j < iter2; j++) {
                Type a = new Type(s.next(), s.nextDouble(), s.nextDouble(), s.nextDouble());
                c.addType(a);
            }
            clss.classes.add(c);
        }
        for (int i = 0; i < clss.abs.length; i++) {
            clss.abs[i] = Integer.parseInt(s.next());
        }
        iter1 = s.nextInt();
        for (int i = 0; i < iter1; i++) {
            clss.schedulesem1.add(s.next());
        }
        iter1 = s.nextInt();
        for (int i = 0; i < iter1; i++) {
            clss.schedulesem2.add(s.next());
        }
        return clss;
    }

    public static ClssPkg getFromServer(String user, String pass, int semnum) {
        String s = "";
        try {
            Socket clientSocket = new Socket("99.8.234.29", 6789);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer.writeBytes(user + " " + pass + '\n');
            String rec = inFromServer.readLine();
            s = rec;
            clientSocket.close();
        } catch (IOException ex) {

        }
        return parse(s,semnum);
    }

}

class Course {

    ArrayList<Assignment> assignments = new ArrayList<Assignment>();
    ArrayList<Type> types = new ArrayList<Type>();
    String name;
    String teacher;
    double grade;
    String email;
    String roomnum;
    String coursenum;
    int periodnum;

    String mp1;
    String mp2;
    String mp3;
    String final1;
    String sem1;

    String mp4;
    String mp5;
    String mp6;
    String final2;
    String sem2;

    public Course(String cour, String t, String e, String r, String cn, int p, String m1, String m2, String m3, String f1, String s1, String m4, String m5, String m6, String f2, String s2) {
        teacher = t;
        email = e;
        roomnum = r;
        coursenum = cn;
        periodnum = p;

        mp1 = m1;
        mp2 = m2;
        mp3 = m3;
        final1 = f1;
        sem1 = s1;

        mp4 = m4;
        mp5 = m5;
        mp6 = m6;
        final2 = f2;
        sem2 = s2;

        name = cour;
    }

    public void addAssign(Assignment a) {
        assignments.add(a);
        calcType();
    }

    public void remAssign(int i) {
        assignments.remove(i);
    }

    public void modAssign(int i, double g) {
        assignments.get(i).grade = "" + g;
    }

    public void addType(Type t) {
        types.add(t);
        calcType();
    }

    public void calcType() {
        double totalgr = 0.0;
        double weightsum = 0.0;
        for (Type t : types) {
            double d = 0.0;
            int pd = 0;
            for (Assignment a : assignments) {
                if (a.type.equals(t.name) && !a.grade.equals("X")) {
                    d += Double.parseDouble(a.grade);
                    pd += Double.parseDouble(a.totalscore);
                }
            }
            totalgr += t.grade = d / pd * t.weight;
            weightsum += t.weight;
        }
        grade = totalgr / weightsum * 100;
    }

    public String toString() {
        String ret = "{" + name + "}"
                + "{" + teacher + "}"
                + "{" + email + "}"
                + "{" + roomnum + "}"
                + "{" + coursenum + "}"
                + "{" + periodnum + "}"
                + "{" + mp1 + "}"
                + "{" + mp2 + "}"
                + "{" + mp3 + "}"
                + "{" + final1 + "}"
                + "{" + sem1 + "}"
                + "{" + mp4 + "}"
                + "{" + mp5 + "}"
                + "{" + mp6 + "}"
                + "{" + final2 + "}"
                + "{" + sem2 + "}";
        ret += assignments.size();
        for (Assignment a : assignments) {
            ret += a.toString();
        }
        ret += "{" + types.size();
        for (Type a : types) {
            ret += a.toString();
        }
        return ret;
    }

    public static Course parse(String str) {
        Scanner s = new Scanner(str);
        s.useDelimiter("\\}\\{|\\}|\\{");
        Course c = new Course(s.next(), s.next(), s.next(), s.next(), s.next(), s.nextInt(), s.next(), s.next(), s.next(), s.next(), s.next(), s.next(), s.next(), s.next(), s.next(), s.next());
        int iter2 = s.nextInt();
        for (int j = 0; j < iter2; j++) {
            Assignment a = new Assignment(s.next(), s.next(), s.next(), s.next(), s.next(), s.next());
            c.addAssign(a);
        }
        iter2 = s.nextInt();
        for (int j = 0; j < iter2; j++) {
            Type a = new Type(s.next(), s.nextDouble(), s.nextDouble(), s.nextDouble());
            c.addType(a);
        }
        return c;
    }

}

class Assignment {

    String duedate;
    String name;
    String type;
    String grade;
    String totalscore;
    String comment = "";

    public Assignment(String d, String n, String t, String g, String ts, String c) {
        duedate = d;
        name = n;
        type = t;
        grade = g;
        totalscore = ts;
        comment = c;
    }

    public String toString() {
        String ret = "{" + duedate + "}"
                + "{" + name + "}"
                + "{" + type + "}"
                + "{" + grade + "}"
                + "{" + totalscore + "}"
                + "{" + comment + "}";
        return ret;
    }

}

class Type implements Comparable {

    String name;
    double weight;
    double grade;
    double origgrade;

    public Type(String n, double w, double o) {
        name = n;
        weight = w;
        origgrade = o;
    }

    public Type(String n, double w, double g, double o) {
        name = n;
        weight = w;
        grade = g;
        origgrade = o;
    }

    @Override
    public int compareTo(Object o) {
        Type t = (Type) o;
        if ((int) (t.weight - weight) != 0) {
            return (int) (t.weight - weight);
        } else {
            return t.name.compareTo(name);
        }
    }

    public String toString() {
        String ret = "{" + name + "}"
                + "{" + weight + "}"
                + "{" + grade + "}"
                + "{" + origgrade + "}";
        return ret;
    }
}
