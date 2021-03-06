package alexa.projectcharizard.ViewModel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import org.apache.commons.validator.routines.EmailValidator;

import alexa.projectcharizard.Model.CurrentRun;
import alexa.projectcharizard.Model.Database;
import alexa.projectcharizard.R;

/**
 * An Android activity class for changing the user email. Uses Apache commons validator
 * in order to validate entered email-addresses. Started by AccountPageActivity.
 */
public class ChangeEmailActivity extends AppCompatActivity {

    private TextView currentEmailView;
    private EditText newEmailView;
    private EditText verifyNewEmailView;

    private Intent intent;
    private DatabaseReference dataReference;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        setContentView(R.layout.activity_change_email);
        initView();
        initText();

        userId = CurrentRun.getActiveUser().getId();

        intent = getIntent();
        dataReference = Database.getInstance().getDatabaseReference().child("Users")
                                .child(userId);

    }

    /**
     * Initialises Android view elements
     */
    private void initView() {
        currentEmailView = (TextView) findViewById(R.id.currentEmailView);
        newEmailView = (EditText) findViewById(R.id.newEmailView);
        verifyNewEmailView = (EditText) findViewById(R.id.verifyNewEmailView);
    }

    /**
     * Sets initial text
     */
    private void initText() {
        String currentEmail = CurrentRun.getActiveUser().getEmail();
        currentEmailView.setText(currentEmail);
    }

    /**
     * Checks so that the new email is valid, not the same as the current one and that
     * the email entered in newEmailField and verifNewEmailField are the same. If the checks pass
     * then calls database to change the user email
     *
     * @param view the view to execute the action in
     */
    public void changeEmailButtonAction(View view) {
        String newEmail = newEmailView.getText().toString();
        String verifMail = verifyNewEmailView.getText().toString();
        if (newEmail.equals(CurrentRun.getActiveUser().getEmail())) {
            // Return error message that the new email is the same as the old one
            Toast.makeText(getApplicationContext(), "New email is the same as the old email", Toast.LENGTH_SHORT).show();
        } else if (isValidEmail(newEmail) && newEmail.equals(verifMail)) {
            // Change the users email address and notify the user'
            dataReference.child("email").setValue(newEmail);
            CurrentRun.getActiveUser().setEmail(newEmail);
            currentEmailView.setText(newEmail);
            Toast.makeText(getApplicationContext(), "Email changed", Toast.LENGTH_SHORT).show();
            finish();
        } else if (isValidEmail(newEmail)) {
            // Return error message that the verification field does not match
            Toast.makeText(getApplicationContext(), "Email does not match", Toast.LENGTH_SHORT).show();
        } else {
            // Return error message that the new email entered is not valid
            Toast.makeText(getApplicationContext(), "Email is not valid", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Uses Apache Commons Validator to determine if a email address is valid
     *
     * @param newEmail the email address to be validated
     * @return boolean
     */
    private boolean isValidEmail(String newEmail) {
        EmailValidator emailValidator = EmailValidator.getInstance();
        return emailValidator.isValid(newEmail);
    }

}
