package br.unisc.caronasuniscegm;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.unisc.caronasuniscegm.model.Message;

public class ChatActivity extends AppCompatActivity {

    private ListView messageListView;
    private MessageListAdapter messageListAdapter;
    private final List<Message> messageList = new ArrayList<Message>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setTitle("João");

        messageListAdapter = new MessageListAdapter();
        messageListView = (ListView) findViewById(R.id.messages_list_view);
        messageListView.setAdapter(messageListAdapter);

        scrollToEnd();
    }

    public void scrollToEnd() {
        messageListView.post(new Runnable() {
            @Override
            public void run() {
                messageListView.setSelection(messageListAdapter.getCount() - 1);
            }
        });
    }

    public void sendMessage(View view) {
        EditText editText = (EditText)findViewById(R.id.chat_message_edit_text);
        String message = editText.getText().toString();

        if (message.isEmpty())
            return;

        editText.setText("");

        messageList.add(new Message(1, message, "Você", "05:30"));
        scrollToEnd();
    }

    public class MessageListAdapter extends ArrayAdapter<Message> {

        public MessageListAdapter() {
            super(ChatActivity.this, R.layout.chat_message_item, messageList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.chat_message_item, parent, false);
            }

            Message message = messageList.get(position);

            TextView author = (TextView)itemView.findViewById(R.id.message_author);
            author.setText(message.getAuthor());

            TextView time = (TextView)itemView.findViewById(R.id.message_time);
            time.setText(message.getDate());

            TextView body = (TextView)itemView.findViewById(R.id.message_body);
            body.setText(message.getBody());

            return itemView;
        }

    }

}
