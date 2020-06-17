package com.example.homework4_6;

import android.Manifest;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.common.io.LittleEndianDataInputStream;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.bloco.faker.Faker;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private File file;
    private List<String> myList;
    private Button btn_display;
    private ListView lv_list_view;
    File list[];
    String filename;
    AdapterView.AdapterContextMenuInfo info;
    ArrayAdapter<String> adapter;
    private Target mTarget;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv_list_view = findViewById(R.id.lv_list_view);
        btn_display = findViewById(R.id.btn_display);
        toolbar = findViewById(R.id.toolbar);
        setEvent();
        myList = new ArrayList<>();

        btn_display.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           if (checkPermission()) {
                               String root_sd = Environment.getExternalStorageDirectory().toString();

                               file = new File( root_sd + "/" ) ;
                               File list[] = file.listFiles();
                               myList.clear();
                               for( int i=0; i< list.length; i++)
                               {
                                   myList.add(list[i].getName());
                               }
                               adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, myList);
                               lv_list_view.setAdapter(adapter);
                           }
                       }
                   });

        lv_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File temp_file = new File( file, myList.get(position));

                if( !temp_file.isFile())
                {
                    file = new File( file, myList.get( position ));
                    list = file.listFiles();

                    myList.clear();

                    for( int i=0; i< list.length; i++)
                    {
                        myList.add( list[i].getName());
                    }
                    Toast.makeText(getApplicationContext(), file.toString(), Toast.LENGTH_LONG).show();
                    lv_list_view.setAdapter(adapter);
                }else{
                   String content = ReadFile(temp_file.getAbsolutePath());
                   displayAlertDialog(content);
                }
            }
        });

        registerForContextMenu(lv_list_view);

    }
    private void setEvent(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        else if(item.getItemId() == R.id.new_file){
            createFile();
            Toast.makeText(MainActivity.this,"Tạo file thành công",Toast.LENGTH_SHORT).show();
        }
        else if(item.getItemId() == R.id.new_folder){
            createFolder();
            Toast.makeText(MainActivity.this,"Tạo thư mục thành công",Toast.LENGTH_SHORT).show();
        }
        else if(item.getItemId() == R.id.new_image){
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        String parent = file.getParent();
        file = new File( parent ) ;
        File list[] = file.listFiles();

        myList.clear();

        for( int i=0; i< list.length; i++)
        {
            myList.add( list[i].getName() );
        }
        Toast.makeText(getApplicationContext(), parent,Toast.LENGTH_LONG).show();
        lv_list_view.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1234)
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Log.v("TAG", "Permission Denied.");
            else
                Log.v("TAG", "Permission Granted.");
    }
    public boolean checkPermission(){
        // Hoi quyen truy nhap tu user
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission Denied. Asking for permission.");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1234);
                return false;
            }
        }
        return true;
    }
    //sao chép
    public void copyDirectory(File sourceLocation , File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[children.length-1]),
                        new File(targetLocation, children[children.length-1]));
            }

        } else {

            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    public void alertDialog(final int id,final String filename){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog dialog = builder.setTitle("Thông Báo")
                .setCancelable(true)
                .setMessage("Bạn có muốn tiếp tục ?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(id ==R.id.Delete){
                            // Xoá thư mục
                            if(!deleteDirectory(new File(file.getAbsolutePath()+"/"+filename))){
                                Toast.makeText(getApplicationContext(),"Không thể xoá thư mục", Toast.LENGTH_LONG).show();

                            }
                                Toast.makeText(getApplicationContext(),"Xoá thư mục thành công", Toast.LENGTH_LONG).show();
                        }
                        else if (id == R.id.ChangeName){
                            finish();
                        }
                    }
                })
                .create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    public void alertDialogFile(final int id,final String filename){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog dialog = builder.setTitle("Thông Báo")
                .setCancelable(true)
                .setMessage("Bạn có muốn tiếp tục ?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(id ==R.id.Delete){
                            // Xoá file
                            if(!deleteDirectory(new File(file.getAbsolutePath()))){
                                Toast.makeText(getApplicationContext(),"Không thể xoá file", Toast.LENGTH_LONG).show();

                            }
                            Toast.makeText(getApplicationContext(),"Xoá file thành công", Toast.LENGTH_LONG).show();
                        }
                        else if (id == R.id.ChangeName){
                            displayAlertDialogRename(filename);
                        }
                        else if (id == R.id.Copy){
                            try {
                                copyDirectory(file,file.getParentFile());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        filename = (String) lv_list_view.getItemAtPosition(info.position);

        if(checkFile(info.position)) {
            menu.setHeaderTitle("Chọn hành động");
            menu.add(Menu.NONE, R.id.Delete,
                    Menu.NONE, R.string.delete);
            menu.add(Menu.NONE, R.id.ChangeName,
                    Menu.NONE, R.string.changename);
            menu.add(Menu.NONE, R.id.Copy,
                    Menu.NONE, R.string.copy);
        }
        else{
            menu.setHeaderTitle("Chọn hành động");
            menu.add(Menu.NONE, R.id.Delete,
                    Menu.NONE, R.string.delete);
            menu.add(Menu.NONE, R.id.ChangeName,
                    Menu.NONE, R.string.changename);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(checkFile(info.position)) {
            if (id == R.id.ChangeName){
                alertDialogFile(id,filename);
            }
            else if (id == R.id.Delete) {
                alertDialogFile(id,filename);
            }
            else if (id == R.id.Copy) {
                alertDialogFile(id,filename);
            }

        }else{
            if (id == R.id.ChangeName)
                alertDialog(id, filename);
            else if (id == R.id.Delete)
                alertDialog(id, filename);
        }

        return super.onContextItemSelected(item);
    }
    //Xoa thu muc
    public boolean deleteDirectory(File path) {

       if(!checkFile(info.position)){
           if( path.exists() ) {
                File[] files = path.listFiles();
                if (files == null) {
                    return true;
                }
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
           }
       }
       else{
           if (path.isDirectory()) {
               File[] files = path.listFiles();
               if (files != null)
                   for (File f : files)
                       deleteDirectory(f);
           }
       }
        return path.delete();
    }
    // doc file
    private String ReadFile(String file_name) {
        String content = null;
        try {
            File file = new File(file_name);
            StringBuilder text = new StringBuilder();

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }
            br.close();
            content = text.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return content;
    }
    //xem noi dung file
    public void displayAlertDialog(final String content) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.custom_dialog,null);
        TextView display_file = alertLayout.findViewById(R.id.tv_display_file);
        display_file.setText(content);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Nội dung file");
        alert.setView(alertLayout);
        alert.setCancelable(true);
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    //sua ten file and folder
    public void displayAlertDialogRename(final String namefile) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.custom_dialog_rename,null);
        final EditText edt_rename = alertLayout.findViewById(R.id.edt_rename);
        edt_rename.setText(namefile);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Đổi tên file");
        alert.setView(alertLayout);
        alert.setCancelable(false);
        alert.setNegativeButton("Huỷ",null);
        alert.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rename(namefile,edt_rename.getText().toString());
            }
        });

        AlertDialog dialog = alert.create();
        dialog.show();
    }
    public void rename(String filename,String fileout){
        File sdcard = Environment.getExternalStorageDirectory();
        File from = new File(sdcard,filename);
        File to = new File(sdcard,fileout);
        from.renameTo(to);
    }
    public boolean checkFile(int position){
        File temp_file = new File( file, myList.get(position));

        if( temp_file.isFile()){
            return true;// là file
        }
        return false;
    }
    public void createFolder(){
        Faker faker = new Faker();
        String dir = file.toString()+ "/"+ faker.name.name();
        File folder = new File(dir);
        folder.mkdirs();
    }

    public void createFile(){
        String dir = file.toString();
        Faker faker = new Faker();
        String file_name =faker.name.name() +".txt";
        File file = new File(dir, file_name);
        FileOutputStream fos;
        byte[] data = new String(faker.lorem.paragraph()).getBytes();
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }
}