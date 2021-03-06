package com.NovaLabs.boodschappenlijstje;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final ArrayList<Item> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //load overwrite list if there is data otherwise keep main list intact
        loadData();

        ListView lv = findViewById(R.id.listview);
        EditText nameInput = findViewById(R.id.item_name_input);
        EditText numberInput = findViewById(R.id.item_number_input);
        Button button = findViewById(R.id.add_button);
        Spinner units_spinner = findViewById(R.id.item_unit_input);

        //set spinner to units.xml
        ArrayAdapter<CharSequence> spinner_adapter = ArrayAdapter.createFromResource(this, R.array.units, android.R.layout.simple_spinner_dropdown_item);
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        units_spinner.setAdapter(spinner_adapter);

        //handle enter as button press
        numberInput.setOnEditorActionListener((textView, i, ke) -> {
            if ((ke != null && (ke.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (i == EditorInfo.IME_ACTION_NEXT)) {
                //do what you want on the press of 'done'
                ButtonClick(nameInput, numberInput, units_spinner);
            }
            return false;
        });

        //need to integrate a lv.update() function somehow
        //edit: DONE! VERY HAPPY RN
        button.setOnClickListener(view -> ButtonClick(nameInput, numberInput, units_spinner));

        lv.setOnItemLongClickListener((adapterView, view, i, l) -> {
            list.remove(i);
            updateList();
            return false;

        });
        numberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                units_spinner.setSelection(1);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }

    private void ButtonClick(EditText nameInput, EditText numberInput, Spinner units_spinner) {
        String name = nameInput.getText().toString();
        String number = numberInput.getText().toString();
        int unit = units_spinner.getSelectedItemPosition();

        list.add(0, new Item(name, number, unit));
        updateList();

        nameInput.setText("");
        numberInput.setText("");
        units_spinner.setSelection(0);
        nameInput.requestFocus();
    }


    private void updateList() {
        //update list
        CustomListAdapter listAdapter = new CustomListAdapter(getApplicationContext(), list);
        ListView lv = findViewById(R.id.listview);
        lv.setAdapter(listAdapter);
    }

    private void loadData(){
        String json = null;
        Intent intent = getIntent();
        Uri uri = intent.getData();
        //get json from intent
        if(intent.getAction().equals("android.intent.action.VIEW") && intent.getType() != null && uri != null){
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
                    json = r.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //or from prefs
        else{
            SharedPreferences sharedPreferences = getSharedPreferences("sp", MODE_PRIVATE);
            json = sharedPreferences.getString("list", null);
        }

        if(json!=null){
            jsonToList(json);
        }
    }

    private void jsonToList(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<String[][]>() {}.getType();
        if(gson.fromJson(json, type) != null){
            String[][] lst = gson.fromJson(json, type);
            for (String[] strings : lst) {
                list.add(new Item(strings[0], strings[1], Integer.parseInt(strings[2])));
            }
            updateList();
        }
    }

    private void deleteData(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(R.string.delete_title);
        alertDialog.setMessage(getText(R.string.delete_message));
        alertDialog.setCancelable(true);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getText(R.string.delete_cancel), (dialog, which) -> dialog.dismiss());
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.delete_button),
                (dialog, which) -> {
                    list.clear();
                    updateList();
                });
        alertDialog.show();
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("sp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = listToJson();
        editor.putString("list", json);
        editor.apply();
    }

    private void shareData(){
        try {
            String json = listToJson();
            File file = new File(getCacheDir(),getString(R.string.share_file_name)+".json");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try (OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream)) {
                writer.write(json);
            } catch (IOException e) {
                e.printStackTrace();
                fileOutputStream.close();
            }

            Uri uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
            Intent shareIntent = new Intent();
            //share uri
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setDataAndType(uri, "text/json");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.title_share));
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooser = Intent.createChooser(shareIntent, getString(R.string.title_share));
            startActivity(chooser);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String listToJson() {
        Gson gson = new Gson();
        String[][] list_items = new String[list.size()][2];
        for(int i = 0; i< list.size(); i++){
            String name = list.get(i).getItemName();
            String number = list.get(i).getItemNumber();
            String unit = String.valueOf(list.get(i).getItemUnit());
            String[] item_values = {name, number, unit};
            list_items[i] = item_values;
        }
        return gson.toJson(list_items);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem men) {
        if(men.getItemId() == R.id.action_save){
            saveData();
            Toast.makeText(this, R.string.saved_state_message, Toast.LENGTH_SHORT).show();
        }
        if(men.getItemId() == R.id.action_share){
            shareData();
        }
        if(men.getItemId() == R.id.action_delete){
            deleteData();
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(super.getIntent().getAction().equals(Intent.ACTION_MAIN)){
            saveData();
        }
    }
}