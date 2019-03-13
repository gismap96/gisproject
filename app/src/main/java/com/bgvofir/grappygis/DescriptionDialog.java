package com.bgvofir.grappygis;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

public class DescriptionDialog extends Dialog implements
    android.view.View.OnClickListener {

  private Activity activity;
  private Button btnConfirm, btnCancel;
  private EditText etDescription, etCategory;
  private Switch isUpdateSwitch;
  IDescriptionDialogListener mListener;

  public DescriptionDialog(Activity a, @NonNull IDescriptionDialogListener listener) {
    super(a);
    // TODO Auto-generated constructor stub
    this.activity = a;
    mListener = listener;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    if (getWindow() != null)
      getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    setContentView(R.layout.description_dialog);
    btnConfirm = findViewById(R.id.btnConfirm);
    btnCancel = findViewById(R.id.btnCancel);
    etDescription = findViewById(R.id.etDescription);
    etCategory = findViewById(R.id.etCategory);
    isUpdateSwitch = findViewById(R.id.isUpdateSwitch);
    btnConfirm.setOnClickListener(this);
    btnCancel.setOnClickListener(this);

  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.btnConfirm:
      boolean isErrors = false;
      etDescription.setError("");
      etCategory.setError("");
      if (etDescription.getText() == null || etDescription.getText().toString().isEmpty()){
        isErrors = true;
        etDescription.setError(activity.getString(R.string.field_mandatory));
      }
      if (etCategory.getText() == null || etCategory.getText().toString().isEmpty()){
        isErrors = true;
        etCategory.setError(activity.getString(R.string.field_mandatory));
      }

      if (!isErrors){
        mListener.onConfirm(etDescription.getText().toString(), etCategory.getText().toString(), isUpdateSwitch.isChecked());
        dismiss();
      }
      break;
    case R.id.btnCancel:
      mListener.onCanceled();
      dismiss();
      break;
    default:
      break;
    }
  }

  interface IDescriptionDialogListener{
    void onConfirm(String description, String category, boolean isUpdateSys);
    void onCanceled();
  }
}