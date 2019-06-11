package com.aspengrades.data;

import android.os.AsyncTask;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

import static com.aspengrades.data.AspenTaskStatus.ASPEN_UNAVAILABLE;
import static com.aspengrades.data.AspenTaskStatus.PARSING_ERROR;
import static com.aspengrades.data.AspenTaskStatus.SUCCESSFUL;

public class ClassList extends ArrayList<SchoolClass> {

    /**
     * The number of terms in the dropdown menu on the classes page
     */
    public static final int NUM_TERMS = 4;

    /**
     * The URL of the "Classes" page
     */
    public static final String CLASSES_URL = "https://aspen.cps.edu/aspen/portalClassList.do?navkey=academics.classes.list";

    /**
     * The code that must be given for "userEvent" when selecting a class
     */
    public static final String CLASS_FORM_EVENT = "2100";

    /**
     * The code that must be given for "userEvent" when selecting a term
     */
    public static final String TERM_SELECT_EVENT = "950";

    /**
     * The codes that must be given for "termFilter" when selecting a term
     */
    public static final String[] TERM_CODES = new String[] {"current", "gtmQ10000000Q1", "gtmQ20000000Q2", "gtmQ30000000Q3", "gtmQ40000000Q4"};

    private int term;
    private String token;
    private AspenTaskStatus status;

    private ClassList(int term, String token, AspenTaskStatus status){
        this.term = term;
        this.token = token;
        this.status = status;
    }

    public static void readClasses(ClassesListener listener, Cookies cookies){
        new ReadClassesTask(listener).execute(cookies);
    }

    public static void readClasses(ClassesListener listener, int term, Cookies cookies){
        new ReadClassesTask(listener, term).execute(cookies);
    }

    public int getTerm(){
        return term;
    }

    public String getToken() {
        return token;
    }

    public AspenTaskStatus getStatus(){
        return status;
    }

    private static class ReadClassesTask extends AsyncTask<Cookies, Void, ClassList>{

        private ClassesListener listener;
        private int term = 0;

        private ReadClassesTask(ClassesListener listener){
            this.listener = listener;
        }

        private ReadClassesTask(ClassesListener listener, int term){
            this.listener = listener;
            this.term = term;
        }

        @Override
        protected final ClassList doInBackground(Cookies... cookies) {
            Document doc;
            try{
                doc = new TermSelector().selectTerm(cookies[0], term);
            }catch (IOException e){
                return new ClassList(term, null, ASPEN_UNAVAILABLE);
            }

            try {
                String token = doc.select("input[name=org.apache.struts.taglib.html.TOKEN]").attr("value");
                ClassList classes = new ClassList(term, token, SUCCESSFUL);
                Element tbody = doc.getElementById("dataGrid").child(0).child(0);
                int[] indexes = getInfoIndexes(tbody.child(0));
                for(int i = 1; i < tbody.children().size() - 1; i++){
                    classes.add(new SchoolClass(tbody.child(i), indexes[0], indexes[1]));
                }
                return classes;
            }
            catch(IndexOutOfBoundsException | NumberFormatException e){
                return new ClassList(term, null, PARSING_ERROR);
            }
        }

        private int[] getInfoIndexes(Element firstRow){
            int[] indexes = new int[] {-1, -1};
            for(int i = 0; i < firstRow.children().size(); i++){
                String text = firstRow.child(i).text();
                if(text.equals("Description")) indexes[0] = i;
                else if(text.equals("Term Performance")) indexes[1] = i;
            }
            return indexes;
        }

        @Override
        protected void onPostExecute(ClassList classList){
            listener.onClassesRead(classList);
        }
    }
}
