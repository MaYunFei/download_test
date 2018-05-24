package com.mayunfei.downloadmanager;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.widget.Toast;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
@LargeTest //重度依赖
public class AndroidFileTest {
    Context context;

    @Before
    public void setUp() throws Exception{
        context = InstrumentationRegistry.getContext();
    }

    @Test
    public void testShouldAddExpenseType() throws Exception {
        File filesDir = context.getFilesDir();
        System.out.println(filesDir.getAbsolutePath());
        Log.e("123","" + filesDir.getAbsolutePath());
//        Toast.makeText(context, "" + filesDir.getAbsolutePath(), Toast.LENGTH_SHORT).show();
    }
}
