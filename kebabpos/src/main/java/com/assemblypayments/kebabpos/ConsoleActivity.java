package com.assemblypayments.kebabpos;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.assemblypayments.utils.SystemHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Simple console-like wrapper around Java sample logic.
 * <p>
 * NOTE: This has nothing to do with the SPI logic, it's merely a convenience
 * to allow code from the Java samples to run on Android to make testing the
 * integration a little simpler. Feel free to ignore!
 */
abstract class ConsoleActivity extends AppCompatActivity {

    private ScrollView statusScrollView;
    private TextView statusTextView;

    private EditText eftposAddressEditText;
    private EditText posIdEditText;

    private ViewGroup actionsContainer;

    private final SystemHelper.ConsoleAdapter consoleAdapter = new SystemHelper.ConsoleAdapter() {
        @Override
        public void print(@NotNull final String text) {
            if (">".equals(text.trim())) {
                // Avoid printing "> " when expecting console input
                return;
            }

            runOnUiThread(new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    statusTextView.setText(statusTextView.getText() + text);
                    statusScrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }

        @Override
        public void println(@NonNull String text) {
            if (Action.validate(text)) {
                addAction(text);
                return;
            }
            print(text + "\n");
        }

        @Override
        public void clear() {
            runOnUiThread(new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    statusTextView.setText("");
                }
            });
        }
    };

    private final View.OnClickListener actionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onActionClicked((Action) v.getTag());
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);

        statusScrollView = findViewById(R.id.scroll_status);
        statusTextView = findViewById(R.id.txt_status);
        eftposAddressEditText = findViewById(R.id.eftpos);
        posIdEditText = findViewById(R.id.posid);
        actionsContainer = findViewById(R.id.container_actions);

        posIdEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setPosId(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        eftposAddressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setEftposAddress(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        SystemHelper.register(consoleAdapter);
    }

    @Override
    protected void onDestroy() {
        SystemHelper.unregister(consoleAdapter);

        super.onDestroy();
    }

    //region Requirements for implementations

    abstract void setPosId(String id);

    abstract void setEftposAddress(String address);

    abstract boolean processInput(String[] spInput);

    //endregion

    protected String[] loadStateArgs(String currentPosId, String currentEftposAddress) {
        String posId = posIdEditText.getText().toString();
        if (TextUtils.isEmpty(posId)) {
            if (!TextUtils.isEmpty(currentPosId)) {
                posId = currentPosId;
            } else {
                posId = "androidpos";
            }
        }
        posIdEditText.setText(posId);

        String eftposAddress = eftposAddressEditText.getText().toString();
        if (TextUtils.isEmpty(eftposAddress)) {
            if (!TextUtils.isEmpty(currentEftposAddress)) {
                eftposAddress = currentEftposAddress;
            } else {
                eftposAddress = "127.0.0.1";
            }
        }
        eftposAddressEditText.setText(eftposAddress);

        return new String[]{posId + ":" + eftposAddress};
    }

    protected void clearActions() {
        actionsContainer.removeAllViews();
    }

    protected void addAction(String text) {
        final Action action = Action.parse(text);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Button button = new Button(ConsoleActivity.this);
                button.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                button.setOnClickListener(actionOnClickListener);
                button.setText(action.getCmd());
                button.setTag(action);
                actionsContainer.addView(button);
            }
        });
    }

    protected void onActionClicked(final Action action) {
        final String[] args = action.getArgs();
        if (args == null || args.length == 0) {
            final String[] spInput = new String[]{action.getCmd()};
            processInput(spInput);
        } else {
            final LinearLayout argsContainer = new LinearLayout(this);
            argsContainer.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            argsContainer.setOrientation(LinearLayout.VERTICAL);

            for (String arg : args) {
                EditText argEdit = new EditText(this);
                argEdit.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                argEdit.setHint(arg);
                argsContainer.addView(argEdit);
            }

            new AlertDialog.Builder(this)
                    .setTitle(action.getCmd())
                    .setMessage(action.getDescription())
                    .setView(argsContainer)
                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final int argCount = argsContainer.getChildCount();
                            final String[] spInput = new String[1 + argCount];

                            spInput[0] = action.getCmd();

                            for (int i = 0; i < argCount; i++) {
                                spInput[i + 1] = ((EditText) argsContainer.getChildAt(i)).getText().toString();
                            }

                            processInput(spInput);
                        }
                    })
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        }
    }

    protected static class Action {

        private static final String DELIMITER_START = "# [";
        private static final String DELIMITER_MID = "] - ";

        private final String cmd;
        private final String description;
        private final String[] args;

        public static boolean validate(String text) {
            return text.startsWith(DELIMITER_START);
        }

        public static Action parse(String text) {
            if (!(text.startsWith(DELIMITER_START) && text.contains(DELIMITER_MID))) {
                throw new IllegalArgumentException("Expected format: # [command:arg1:arg2] - description of command");
            }

            final String[] sections = text.replace(DELIMITER_START, "").split(DELIMITER_MID);
            if (sections.length != 2) {
                throw new IllegalArgumentException("Exactly one argument block and one description expected");
            }

            final String description = sections[1];

            final String[] cmdWithArgs = sections[0].split(":");
            if (cmdWithArgs.length < 1) {
                throw new IllegalArgumentException("Command name required (arguments optional)");
            }

            final String cmd = cmdWithArgs[0];

            final String[] args = Arrays.copyOfRange(cmdWithArgs, 1, cmdWithArgs.length);

            return new Action(cmd, description, args);
        }

        private Action(String cmd, String description, String[] args) {
            this.cmd = cmd;
            this.description = description;
            this.args = args;
        }

        public String getCmd() {
            return cmd;
        }

        public String getDescription() {
            return description;
        }

        public String[] getArgs() {
            return args;
        }

    }

}
