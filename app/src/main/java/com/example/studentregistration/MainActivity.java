package com.example.studentregistration;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;

    private RadioGroup rgGrade;
    private RadioButton rbGrade10;
    private RadioButton rbGrade11;
    private RadioButton rbGrade12;

    private Spinner spMajor;

    private CheckBox cbNotifications;
    private CheckBox cbTerms;

    private TextView tvResult;
    private TextView tvGradeError;
    private TextView tvMajorError;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // חיבור רכיבי המסך
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);

        rgGrade = findViewById(R.id.rgGrade);
        rbGrade10 = findViewById(R.id.rbGrade10);
        rbGrade11 = findViewById(R.id.rbGrade11);
        rbGrade12 = findViewById(R.id.rbGrade12);

        spMajor = findViewById(R.id.spMajor);

        cbNotifications = findViewById(R.id.cbNotifications);
        cbTerms = findViewById(R.id.cbTerms);

        Button btnRegister = findViewById(R.id.btnRegister);
        tvResult = findViewById(R.id.tvResult);
        tvGradeError = findViewById(R.id.tvGradeError);
        tvMajorError = findViewById(R.id.tvMajorError);

        // הגדרת אפשרויות המגמה
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.majors,
                        android.R.layout.simple_spinner_item
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spMajor.setAdapter(adapter);

        // פתיחת SharedPreferences
        preferences = getSharedPreferences(
                "StudentData",
                MODE_PRIVATE
        );

        // טעינת נתונים מהפעלה קודמת
        loadStudent();

        // לחיצה על כפתור ההרשמה
        btnRegister.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {

        // איפוס הודעות שגיאה קודמות
        etFirstName.setError(null);
        etLastName.setError(null);
        etEmail.setError(null);
        cbTerms.setError(null);

        tvGradeError.setVisibility(View.GONE);
        tvMajorError.setVisibility(View.GONE);

        // קליטת נתונים מהמסך
        String firstName =
                etFirstName.getText().toString().trim();

        String lastName =
                etLastName.getText().toString().trim();

        String email =
                etEmail.getText().toString().trim();

        // בדיקת שם פרטי
        if (firstName.isEmpty()) {
            etFirstName.setError(
                    getString(R.string.error_first_name)
            );
        }

        // בדיקת שם משפחה
        if (lastName.isEmpty()) {
            etLastName.setError(
                    getString(R.string.error_last_name)
            );
        }

        // בדיקת אימייל
        if (email.isEmpty()) {
            etEmail.setError(
                    getString(R.string.error_email_empty)
            );
        } else if (!Patterns.EMAIL_ADDRESS
                .matcher(email).matches()) {

            etEmail.setError(
                    getString(R.string.error_email_invalid)
            );
        }

        // בדיקת כיתה
        String grade;
        int selectedGradeId = rgGrade.getCheckedRadioButtonId();
        if (selectedGradeId == -1) {
            tvGradeError.setVisibility(View.VISIBLE);
            grade = "";
        } else {
            RadioButton selectedGrade = findViewById(selectedGradeId);
            grade = (selectedGrade != null) ? selectedGrade.getText().toString() : "";
        }

        // בדיקת מגמה
        String major;
        if (spMajor.getSelectedItemPosition() == 0) {
            tvMajorError.setVisibility(View.VISIBLE);
            major = "";
        } else {
            major = spMajor.getSelectedItem().toString();
        }

        // בדיקת אישור תנאי השימוש
        boolean termsAccepted = cbTerms.isChecked();
        if (!termsAccepted) {
            cbTerms.setError(
                    getString(R.string.error_terms)
            );
        }

        // אם יש שגיאה, לא מציגים ולא שומרים נתונים
        boolean valid = !firstName.isEmpty()
                && !lastName.isEmpty()
                && !email.isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(email).matches()
                && !grade.isEmpty()
                && !major.isEmpty()
                && termsAccepted;

        if (!valid) {
            return;
        }

        // יצירת אובייקט תלמיד
        Student student = new Student(
                firstName,
                lastName,
                email,
                grade,
                major,
                cbNotifications.isChecked(),
                true
        );

        // שמירת האובייקט ב-SharedPreferences
        saveStudent(student);

        // הצגת פרטי התלמיד
        displayStudent(student);
    }

    private void saveStudent(Student student) {

        SharedPreferences.Editor editor =
                preferences.edit();

        editor.putString(
                "firstName", student.getFirstName()
        );

        editor.putString(
                "lastName", student.getLastName()
        );

        editor.putString(
                "email", student.getEmail()
        );

        editor.putString(
                "grade", student.getGrade()
        );

        editor.putString(
                "major", student.getMajor()
        );

        editor.putBoolean(
                "notifications", student.isNotifications()
        );

        editor.putBoolean(
                "termsAccepted", student.isTermsAccepted()
        );

        editor.apply();
    }

    private void loadStudent() {

        // אם לא נשמרו נתונים, לא מציגים תוצאה
        if (!preferences.contains("firstName")) {
            tvResult.setVisibility(View.GONE);
            return;
        }

        // טעינת הנתונים השמורים
        Student student = new Student(
                preferences.getString("firstName", ""),
                preferences.getString("lastName", ""),
                preferences.getString("email", ""),
                preferences.getString("grade", ""),
                preferences.getString("major", ""),
                preferences.getBoolean("notifications", false),
                preferences.getBoolean("termsAccepted", false)
        );

        // מילוי מחדש של הטופס
        etFirstName.setText(student.getFirstName());
        etLastName.setText(student.getLastName());
        etEmail.setText(student.getEmail());

        if (Objects.equals(student.getGrade(), rbGrade10.getText().toString())) {
            rbGrade10.setChecked(true);
        } else if (Objects.equals(student.getGrade(), rbGrade11.getText().toString())) {
            rbGrade11.setChecked(true);
        } else if (Objects.equals(student.getGrade(), rbGrade12.getText().toString())) {
            rbGrade12.setChecked(true);
        }

        @SuppressWarnings("unchecked")
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spMajor.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(student.getMajor());
            if (position >= 0) {
                spMajor.setSelection(position);
            }
        }

        cbNotifications.setChecked(student.isNotifications());
        cbTerms.setChecked(student.isTermsAccepted());

        displayStudent(student);
    }

    private void displayStudent(Student student) {

        String notificationText;

        if (student.isNotifications()) {
            notificationText =
                    getString(R.string.result_notifications_yes);
        } else {
            notificationText =
                    getString(R.string.result_notifications_no);
        }

        String result =
                getString(R.string.result_title) + "\n"
                        + getString(
                        R.string.result_first_name,
                        student.getFirstName()
                ) + "\n"
                        + getString(
                        R.string.result_last_name,
                        student.getLastName()
                ) + "\n"
                        + getString(
                        R.string.result_email,
                        student.getEmail()
                ) + "\n"
                        + getString(
                        R.string.result_grade,
                        student.getGrade()
                ) + "\n"
                        + getString(
                        R.string.result_major,
                        student.getMajor()
                ) + "\n"
                        + notificationText + "\n"
                        + getString(R.string.result_terms_yes);

        tvResult.setText(result);
        tvResult.setVisibility(View.VISIBLE);
    }
}