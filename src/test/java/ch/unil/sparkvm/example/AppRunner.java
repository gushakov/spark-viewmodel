package ch.unil.sparkvm.example;

import ch.unil.sparkvm.SparkViewModel;

/**
 * @author gushakov
 */
public class AppRunner {

    public static void main(String[] args) {
        SparkViewModel.run(AppRunner.class.getPackage().getName());
    }

}
