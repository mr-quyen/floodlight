package net.floodlightcontroller.crossfire;

import org.python.antlr.ast.Str;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by quyen on 24/6/16.
 */
public class Test {
    public static void main(String[] args) {
        List<String> stringTest1 = new ArrayList<String>();
        List<String> stringTest2 = new ArrayList<String>();
        stringTest1.add("1");
        stringTest2.add("2");

        int i ;
        i = 4> 13? 1:20;
        i = 1<<(32-12);
        System.out.println("results is: " + i);
        Merge merge = new Merge(stringTest1, stringTest2);
        merge.update();
        for (String e :
                stringTest1) {
            System.out.println(e);
        }
        for (String e :
                stringTest2) {
            System.out.println(e);
        }
    }

}

class Merge{

    List<String> strings1;
    List<String> strings2;
    public Merge(List<String> strings1, List<String> strings2) {
        this.strings1 = strings1;
        this.strings2 = strings2;

    }
    public void update(){
        this.strings1.add("Quyen");
        this.strings2.add("Tuoi");
    }
}
