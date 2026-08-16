package com.snhu.eventtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Sign-in screen. Checks credentials against the SQLite {@code users} table and,
 * for first-time users, creates a new account that is saved to the database.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userRepository = new UserRepository(this);

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        MaterialButton btnLogin = findViewById(R.id.btn_login);
        MaterialButton btnCreate = findViewById(R.id.btn_create_account);

        btnLogin.setOnClickListener(this::onLoginClicked);
        btnCreate.setOnClickListener(this::onCreateAccountClicked);
    }

    private void onLoginClicked(View v) {
        if (!fieldsPresent()) return;
        // Check the typed credentials against the saved accounts.
        long userId = userRepository.authenticate(currentUsername(), currentPassword());
        if (userId == -1) {
            Toast.makeText(this, R.string.msg_login_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        openDashboard(userId);
    }

    private void onCreateAccountClicked(View v) {
        if (!fieldsPresent()) return;
        // Save the new account to the database. -1 means the username is taken.
        long userId = userRepository.createUser(currentUsername(), currentPassword());
        if (userId == -1) {
            Toast.makeText(this, R.string.msg_username_taken, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, R.string.msg_account_created, Toast.LENGTH_SHORT).show();
        openDashboard(userId);
    }

    private boolean fieldsPresent() {
        String username = currentUsername();
        String password = currentPassword();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.msg_login_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!InputValidator.isUsernameValid(username)) {
            Toast.makeText(this, "Username must be at least 3 characters and use letters, numbers, dots, dashes, or underscores.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!InputValidator.isPasswordValid(password)) {
            Toast.makeText(this, "Password must be at least 8 characters.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private String currentUsername() {
        return usernameInput.getText() == null ? "" : InputValidator.normalizeUsername(usernameInput.getText().toString());
    }

    private String currentPassword() {
        return passwordInput.getText() == null ? "" : passwordInput.getText().toString();
    }

    private void openDashboard(long userId) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra(DashboardActivity.EXTRA_USER_ID, userId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();   // remove Login from the back stack so the system Back button exits the app
    }
}
