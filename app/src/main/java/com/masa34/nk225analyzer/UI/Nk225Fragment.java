package com.masa34.nk225analyzer.UI;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.masa34.nk225analyzer.R;
import com.masa34.nk225analyzer.Stock.Nk225Entity;

public class Nk225Fragment extends Fragment {
    private final String TAG = "Nk225Fragment";

    public Nk225Fragment() {
        Log.d(TAG, "Nk225Fragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_nk225, container, false);

        Bundle bundle = getArguments();
        Nk225Entity nk225 = (Nk225Entity)bundle.getSerializable("NK225");
        Nk225ListAdapter adapter = new Nk225ListAdapter(getActivity(), nk225);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }
}
