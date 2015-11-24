package br.unisc.caronasuniscegm;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.unisc.caronasuniscegm.model.Message;

public class ChatActivity extends AppCompatActivity {

    private final List<Message> messageList = new ArrayList<Message>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setTitle("João");

        final ListView lv = (ListView) findViewById(R.id.messages_list_view);

        // Instanciating an array list (you don't need to do this,
        // you already have yours).
        messageList.add(new Message(1, "olar", "Gui", "05:30"));
        messageList.add(new Message(1, "olar", "Gui", "05:30"));
        messageList.add(new Message(1, "olar", "Gui", "05:30"));
        messageList.add(new Message(1, "olar", "Gui", "05:30"));
        messageList.add(new Message(1, "olar", "Gui", "05:30"));
        messageList.add(new Message(1, "olar", "Gui", "05:30"));
        messageList.add(new Message(1, "olar", "Gui", "05:30"));
        messageList.add(new Message(1, "olar", "Gui", "05:30"));
        messageList.add(new Message(1, "olar", "Gui", "05:30"));
        messageList.add(new Message(1, "olar", "Gui", "05:30"));
        messageList.add(new Message(1, "olar", "Gui", "05:30"));
        messageList.add(new Message(1, "olar", "Gui", "05:30"));
        messageList.add(new Message(1, "olar", "Gui", "05:30"));
        messageList.add(new Message(1, "olar", "Gui", "05:30"));

        final MessageListAdapter adapter = new MessageListAdapter();
        lv.setAdapter(adapter);

        lv.post(new Runnable() {
            @Override
            public void run() {
                lv.setSelection(adapter.getCount() - 1);
            }
        });
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
