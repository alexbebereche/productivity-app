package ro.ase.csie.licenta.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ro.ase.csie.licenta.R;
import ro.ase.csie.licenta.activities.ChatBotActivity;
import ro.ase.csie.licenta.classes.bot.Message;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

  private List<Message> messageList;
  private Activity activity;

  public ChatAdapter(List<Message> messageList, Activity activity) {
    this.messageList = messageList;
    this.activity = activity;
  }


  @NonNull
  @Override
  public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(activity).inflate(R.layout.adapter_chat_messages, parent, false);
    return new MyViewHolder(view);
  }

  @Override public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
    String message = messageList.get(position).getMessage();
    boolean isReceived = messageList.get(position).isReceived();
     if(isReceived){
       holder.messageReceive.setVisibility(View.VISIBLE);
       holder.messageSend.setVisibility(View.GONE);


       if(message.startsWith("Adding an alarm for")){
         holder.messageReceive.setText(message.substring(0, message.length() - 3));
       }
       else if(message.startsWith("I will start a timer for")){
         holder.messageReceive.setText("I will start a timer for you.");
       }
       else{
         holder.messageReceive.setText(message);
       }

       holder.cvReceive.setVisibility(View.VISIBLE);
       holder.llReceive.setVisibility(View.VISIBLE);
       holder.imgReceive.setVisibility(View.VISIBLE);

       holder.cvSend.setVisibility(View.GONE);
       holder.llSend.setVisibility(View.GONE);
       holder.imgSend.setVisibility(View.GONE);

     }else {
       holder.messageSend.setVisibility(View.VISIBLE);
       holder.messageReceive.setVisibility(View.GONE);
       holder.messageSend.setText(message);

       holder.cvSend.setVisibility(View.VISIBLE);
       holder.llSend.setVisibility(View.VISIBLE);
       holder.imgSend.setVisibility(View.VISIBLE);

       holder.cvReceive.setVisibility(View.GONE);
       holder.llReceive.setVisibility(View.GONE);
       holder.imgReceive.setVisibility(View.GONE);


     }
  }

  @Override public int getItemCount() {
    return messageList.size();
  }

  static class MyViewHolder extends RecyclerView.ViewHolder{

    TextView messageSend;
    TextView messageReceive;

    CardView cvSend;
    CardView cvReceive;

    LinearLayout llSend;
    LinearLayout llReceive;

    ImageView imgSend;
    ImageView imgReceive;

    MyViewHolder(@NonNull View itemView) {
      super(itemView);
      messageSend = itemView.findViewById(R.id.message_send);
      messageReceive = itemView.findViewById(R.id.message_receive);

      cvSend = itemView.findViewById(R.id.cvSend);
      cvReceive = itemView.findViewById(R.id.cvReceive);

      llSend = itemView.findViewById(R.id.llSend);
      llReceive = itemView.findViewById(R.id.llReceive);

      imgSend = itemView.findViewById(R.id.imgSend);
      imgReceive = itemView.findViewById(R.id.imgReceive);
    }
    
  }

  private String extractInt(String sentence)
  {
    sentence = sentence.replaceAll("[^\\d]", " ");
    sentence = sentence.trim();
    sentence = sentence.replaceAll(" +", " ");
    if (sentence.equals(""))
      return "-1";

    return sentence;
  }

}


