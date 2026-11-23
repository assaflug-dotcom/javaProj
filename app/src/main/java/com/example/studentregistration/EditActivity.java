package com.example.studentregistration;

import android.content.Intent;
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

public class EditActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail;
    private RadioGroup rgGrade;
    private RadioButton rbGrade10, rbGrade11, rbGrade12;
    private Spinner spMajor;
    private CheckBox cbNotifications, cbTerms;
    private TextView tvGradeError, tvMajorError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        etFirstName = findViewById(R.id.etEditFirstName);
        etLastName = findViewById(R.id.etEditLastName);
        etEmail = findViewById(R.id.etEditEmail);

        rgGrade = findViewById(R.id.rgEditGrade);
        rbGrade10 = findViewById(R.id.rbEditGrade10);
        rbGrade11 = findViewById(R.id.rbEditGrade11);
        rbGrade12 = findViewById(R.id.rbEditGrade12);

        spMajor = findViewById(R.id.spEditMajor);

        cbNotifications = findViewById(R.id.cbEditNotifications);
        cbTerms = findViewById(R.id.cbEditTerms);

        tvGradeError = findViewById(R.id.tvEditGradeError);
        tvMajorError = findViewById(R.id.tvEditMajorError);

        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnSaveReturn = findViewById(R.id.btnSaveReturn);

        // טעינת רשימת המגמות לספינר
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.majors,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMajor.setAdapter(adapter);

        // קבלת נתוני התלמיד הקיימים ומילוי הטופס
        if (getIntent() != null && getIntent().hasExtra("student")) {
            Student student = (Student) getIntent().getSerializableExtra("student");
            if (student != null) {
                populateForm(student);
            }
        }

        // כפתור ביטול - מחזיר RESULT_CANCELED מבלי לשנות דבר
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // כפתור שמירה וחזור - בדיקת תקינות ושליחת התוצאה בחזרה
        btnSaveReturn.setOnClickListener(v -> saveAndReturn());
    }

    private void populateForm(Student student) {
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
            int pos = adapter.getPosition(student.getMajor());
            if (pos >= 0) {
                spMajor.setSelection(pos);
            }
        }

        cbNotifications.setChecked(student.isNotifications());
        cbTerms.setChecked(student.isTermsAccepted());
    }

    private void saveAndReturn() {
        etFirstName.setError(null);
        etLastName.setError(null);
        etEmail.setError(null);
        cbTerms.setError(null);
        tvGradeError.setVisibility(View.GONE);
        tvMajorError.setVisibility(View.GONE);

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (firstName.isEmpty()) {
            etFirstName.setError(getString(R.string.error_first_name));
        }
        if (lastName.isEmpty()) {
            etLastName.setError(getString(R.string.error_last_name));
        }
        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.error_email_empty));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_email_invalid));
        }

        String grade;
        int selectedGradeId = rgGrade.getCheckedRadioButtonId();
        if (selectedGradeId == -1) {
            tvGradeError.setVisibility(View.VISIBLE);
            grade = "";
        } else {
            RadioButton rb = findViewById(selectedGradeId);
            grade = (rb != null) ? rb.getText().toString() : "";
        }

        String major;
        if (spMajor.getSelectedItemPosition() == 0) {
            tvMajorError.setVisibility(View.VISIBLE);
            major = "";
        } else {
            major = spMajor.getSelectedItem().toString();
        }

        boolean termsAccepted = cbTerms.isChecked();
        if (!termsAccepted) {
            cbTerms.setError(getString(R.string.error_terms));
        }

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

        Student updatedStudent = new Student(
                firstName,
                lastName,
                email,
                grade,
                major,
                cbNotifications.isChecked(),
                true
        );

        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedStudent", updatedStudent);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}