package com.example.studentregistration;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class DetailsActivity extends AppCompatActivity {

    private Student currentStudent;
    private TextView tvDetailsContent;
    private SharedPreferences preferences;
    private ActivityResultLauncher<Intent> editLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        tvDetailsContent = findViewById(R.id.tvDetailsContent);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnEmail = findViewById(R.id.btnEmail);
        Button btnShare = findViewById(R.id.btnShare);

        preferences = getSharedPreferences("StudentData", MODE_PRIVATE);

        // קבלת אובייקט התלמיד שנשלח מ-MainActivity
        if (getIntent() != null && getIntent().hasExtra("student")) {
            currentStudent = (Student) getIntent().getSerializableExtra("student");
        }

        if (currentStudent != null) {
            displayStudent(currentStudent);
        }

        // הגדרת ActivityResultLauncher לחזרה ממסך העריכה עם תוצאה מעודכנת
        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Student updatedStudent = (Student) result.getData().getSerializableExtra("updatedStudent");
                        if (updatedStudent != null) {
                            currentStudent = updatedStudent;
                            displayStudent(currentStudent);
                            saveStudentToPrefs(currentStudent);
                        }
                    }
                }
        );

        // מעבר למסך העריכה
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(DetailsActivity.this, EditActivity.class);
            intent.putExtra("student", currentStudent);
            editLauncher.launch(intent);
        });

        // שליחת מייל
        btnEmail.setOnClickListener(v -> sendEmail());

        // שיתוף פרטים
        btnShare.setOnClickListener(v -> shareDetails());
    }

    private void displayStudent(Student student) {
        String notificationText = student.isNotifications() ?
                getString(R.string.result_notifications_yes) :
                getString(R.string.result_notifications_no);

        String details = getString(R.string.result_title) + "\n" +
                getString(R.string.result_first_name, student.getFirstName()) + "\n" +
                getString(R.string.result_last_name, student.getLastName()) + "\n" +
                getString(R.string.result_email, student.getEmail()) + "\n" +
                getString(R.string.result_grade, student.getGrade()) + "\n" +
                getString(R.string.result_major, student.getMajor()) + "\n" +
                notificationText + "\n" +
                getString(R.string.result_terms_yes);

        tvDetailsContent.setText(details);
    }

    private void saveStudentToPrefs(Student student) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("firstName", student.getFirstName());
        editor.putString("lastName", student.getLastName());
        editor.putString("email", student.getEmail());
        editor.putString("grade", student.getGrade());
        editor.putString("major", student.getMajor());
        editor.putBoolean("notifications", student.isNotifications());
        editor.putBoolean("termsAccepted", student.isTermsAccepted());
        editor.apply();
    }

    private void sendEmail() {
        if (currentStudent == null) return;

        String notificationText = currentStudent.isNotifications() ?
                getString(R.string.result_notifications_yes) :
                getString(R.string.result_notifications_no);

        String body = String.format(
                getString(R.string.email_body_template),
                currentStudent.getFirstName(),
                currentStudent.getLastName(),
                currentStudent.getEmail(),
                currentStudent.getGrade(),
                currentStudent.getMajor(),
                notificationText
        );

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + currentStudent.getEmail()));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        intent.putExtra(Intent.EXTRA_TEXT, body);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.email_no_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareDetails() {
        if (currentStudent == null) return;

        String notificationText = currentStudent.isNotifications() ?
                getString(R.string.result_notifications_yes) :
                getString(R.string.result_notifications_no);

        String shareText = getString(R.string.result_title) + "\n" +
                getString(R.string.result_first_name, currentStudent.getFirstName()) + "\n" +
                getString(R.string.result_last_name, currentStudent.getLastName()) + "\n" +
                getString(R.string.result_email, currentStudent.getEmail()) + "\n" +
                getString(R.string.result_grade, currentStudent.getGrade()) + "\n" +
                getString(R.string.result_major, currentStudent.getMajor()) + "\n" +
                notificationText + "\n" +
                getString(R.string.result_terms_yes);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);

        Intent chooser = Intent.createChooser(intent, getString(R.string.share_title));
        if (chooser.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(this, R.string.share_no_app, Toast.LENGTH_SHORT).show();
        }
    }
}