package listener.main;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Preprocess {
    static String preprocess(String filename) throws Exception{
        String procname = filename.replaceAll(".c", ".aa");
        BufferedReader br = new BufferedReader(new FileReader(filename));
        BufferedWriter bw = new BufferedWriter(new FileWriter(procname));
        ArrayList<String> libfileList = getLibfiles(br);
        AddLibFiles(bw, filename, libfileList);
        AddSourcefile(br, bw);
        br.close();
        bw.close();
        return procname;
    }
    static void AddSourcefile(BufferedReader br, BufferedWriter bw) throws Exception{
        while(br.ready()) {
            bw.write(br.readLine());
            bw.write("\n");
        }
    }
    static void AddLibFiles(BufferedWriter bw, String filename, ArrayList<String> libfileList) throws  Exception{
        String[] list = filename.split("/");
        String prefix = "";
        for(int i=0;i<list.length-1;i++)
            prefix += list[i] + "/";
        for(String libFile : libfileList){
            BufferedReader br = new BufferedReader(new FileReader(prefix+libFile));
            while(br.ready()) {
                String line = br.readLine();
                bw.write(line);
                bw.write('\n');
            }
            br.close();
        }
    }
    static ArrayList<String> getLibfiles(BufferedReader br) throws Exception{
        ArrayList<String> list = new ArrayList<String>();
        while(true) {
            br.mark(1);
            String line = br.readLine();
            if (line == null) break;

            StringTokenizer st = new StringTokenizer(line);
            if (!st.hasMoreTokens()) continue;

            String s = st.nextToken();
            if (s.charAt(0) != '#') {
                br.reset();
                break;
            }
            String fileName = st.nextToken().replaceAll("\"", "");
            list.add(fileName);
        }
        return list;
    }
    public static void main(String[] args) throws Exception{
        String fileName = "test/test10.c";
        Preprocess.preprocess(fileName);
    }
}
