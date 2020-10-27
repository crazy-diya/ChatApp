package com.example.whatsappmyapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GroupsFragment extends Fragment {

    private View groupFragmentView;

    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_group = new ArrayList<>();

    private DatabaseReference GroupRef;

    public GroupsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        GroupRef = FirebaseDatabase.getInstance().getReference("Groups");

        InitializeFields();
        
        RetriveAndDisplayGroup();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupName = parent.getItemAtPosition(position).toString();

                Intent groupChatIntent = new Intent(getContext(),GroupChatActivity.class);
                groupChatIntent.putExtra("groupName",currentGroupName);
                startActivity(groupChatIntent);
            }
        });

        return groupFragmentView;
    }


    private void InitializeFields() {
        listView = (ListView) groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,list_of_group);
        listView.setAdapter(arrayAdapter);
    }

    private void RetriveAndDisplayGroup() {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set set = new HashSet();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    set.add(dataSnapshot1.getKey());
                }
                list_of_group.clear();
                list_of_group.addAll(set);
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}