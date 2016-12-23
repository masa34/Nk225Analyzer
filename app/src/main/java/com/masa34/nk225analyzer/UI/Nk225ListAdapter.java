package com.masa34.nk225analyzer.UI;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.masa34.nk225analyzer.R;
import com.masa34.nk225analyzer.Stock.Nk225Entity;
import com.masa34.nk225analyzer.UI.Card.BollingerBandCard;
import com.masa34.nk225analyzer.UI.Card.ComprehensiveEvaluationCard;
import com.masa34.nk225analyzer.UI.Card.EstrangementRateCard;
import com.masa34.nk225analyzer.UI.Card.EvaluationViewHolder;
import com.masa34.nk225analyzer.UI.Card.LosersRatioCard;
import com.masa34.nk225analyzer.UI.Card.MovingAverageCard;
import com.masa34.nk225analyzer.UI.Card.Nk225Card;
import com.masa34.nk225analyzer.UI.Card.Nk225CardBase;
import com.masa34.nk225analyzer.UI.Card.PsychologicalCard;
import com.masa34.nk225analyzer.UI.Card.RciCard;
import com.masa34.nk225analyzer.UI.Card.RsiCard;
import com.masa34.nk225analyzer.UI.Card.ViewHolder;
import com.masa34.nk225analyzer.UI.Card.ViewHolder1;
import com.masa34.nk225analyzer.UI.Card.ViewHolder2;

import java.util.ArrayList;
import java.util.List;

public class Nk225ListAdapter extends RecyclerView.Adapter<ViewHolder> {

    private final String TAG = "Nk225ListAdapter";

    private Context context;
    private List<Nk225CardBase> cardList = new ArrayList<Nk225CardBase>();

    public Nk225ListAdapter(Context context, Nk225Entity entity) {
        Log.d(TAG, "Nk225ListAdapter");

        this.context = context;
        cardList.add(new ComprehensiveEvaluationCard(entity));
        cardList.add(new Nk225Card(entity));
        cardList.add(new EstrangementRateCard(entity));
        cardList.add(new LosersRatioCard(entity));
        cardList.add(new RsiCard(entity));
        cardList.add(new RciCard(entity));
        cardList.add(new BollingerBandCard(entity));
        cardList.add(new PsychologicalCard(entity));
        //cardList.add(new StochasticsCard(entity));
        cardList.add(new MovingAverageCard(entity));
        //cardList.add(new MacdCard(entity));
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount");
        return cardList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType");
        return cardList.get(position).getItemViewType();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        switch (viewType) {
            case Nk225CardBase.TYPE_EVALUATION:
                return new EvaluationViewHolder(inflater.inflate(R.layout.evaluation_card, parent, false));

            case Nk225CardBase.TYPE_ITEM1:
                return new ViewHolder1(inflater.inflate(R.layout.card_item1, parent, false));

            case Nk225CardBase.TYPE_ITEM2:
                return new ViewHolder2(inflater.inflate(R.layout.card_item2, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");
        cardList.get(position).bindViewHolder(holder);
    }
}