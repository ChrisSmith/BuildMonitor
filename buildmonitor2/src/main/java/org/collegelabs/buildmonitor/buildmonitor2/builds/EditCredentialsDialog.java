package org.collegelabs.buildmonitor.buildmonitor2.builds;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.okhttp.HttpUrl;
import org.collegelabs.buildmonitor.buildmonitor2.BuildMonitorApplication;
import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.tc.Credentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.ServerResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.ServiceHelper;
import org.collegelabs.buildmonitor.buildmonitor2.util.RxUtil;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 */
public class EditCredentialsDialog extends DialogFragment {

    @InjectView(R.id.edit_credentials_username) EditText _username;
    @InjectView(R.id.edit_credentials_serverurl) EditText _server;
    @InjectView(R.id.edit_credentials_password) EditText _password;
    @InjectView(R.id.edit_credentials_isguest) Switch _isGuest;
    @InjectView(R.id.edit_credentials_test_button) Button _validateButton;
    @InjectView(R.id.edit_credentials_test_result) TextView _validateResult;

    private Subscription _subscription;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View v = LayoutInflater.from(getContext()).inflate(R.layout.edit_credentials_dialog, null);
        ButterKnife.inject(this, v);

        _validateButton.setOnClickListener(this::ValidateCredentials);

        _isGuest.setOnCheckedChangeListener((buttonView, isChecked) -> {
            _password.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            _username.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });

        builder.setMessage("Server Credentials")
                .setPositiveButton(android.R.string.ok, (dialog, id) -> { })
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> { })
                .setView(v);

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unsubscribe();
    }

    private void ValidateCredentials(View view) {
        unsubscribe();

        Validation validation = validate();
        if(validation.isValid()){
            Credentials c = validation.isGuest
                    ? new Credentials(-1, validation.serverUrl)
                    : new Credentials(-1, validation.username, validation.password, validation.serverUrl);

            _subscription = ServiceHelper.getService(c).getServer()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(ValidationViewModel::new)
                    .startWith(new ValidationViewModel("Loading..."))
                    .subscribe(model -> {
                        _validateResult.setText(model.text);
                    }, e -> {
                        _validateResult.setText(e.getMessage());
                    });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {

                Validation validation = validate();
                if(validation.isValid()){
                    saveCredentials(validation);
                    d.dismiss();
                }

            });
        }
    }

    private void unsubscribe() {
        RxUtil.unsubscribe(_subscription);
        _subscription = null;
    }

    private Validation validate(){

        final String username = _username.getText().toString();
        final String password = _password.getText().toString();
        final String serverUrl = _server.getText().toString();
        final boolean isGuest = _isGuest.isChecked();

        boolean isValid = true;

        if(serverUrl.length() == 0){
            _server.setError("Server URL is required");
            isValid = false;
        }else if(HttpUrl.parse(serverUrl) == null){
            _server.setError("Invalid URL");
            isValid = false;
        }

        if(!isGuest){
            if(username.length() == 0){
                _username.setError("Username is required");
                isValid = false;
            }

            if(password.length() == 0){
                _password.setError("Password is required");
                isValid = false;
            }
        }

        return new Validation(username, password, serverUrl, isGuest, isValid);
    }

    private void saveCredentials(final Validation validation) {

        Observable.just(0)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(x -> BuildMonitorApplication.Db.InsertCredentials(validation.username, validation.serverUrl, validation.password, validation.isGuest))
                .subscribe(); // Is this bad? potential leak here
    }

    private static class Validation {
        public final String username, password, serverUrl;
        public final boolean isGuest;
        private boolean isValid;

        public Validation(String username, String password, String serverUrl, boolean isGuest, boolean isValid){
            this.username = username;
            this.password = password;
            this.serverUrl = serverUrl;
            this.isGuest = isGuest;
            this.isValid = isValid;
        }

        public boolean isValid() {
            return isValid;
        }
    }

    private static class ValidationViewModel {
        public String text;

        public ValidationViewModel(String text){
            this.text = text;
        }

        public ValidationViewModel(ServerResponse response){
            this.text = "Success. Server Version: " + response.version;
        }
    }
}
