package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	private PremierLeagueDAO dao;
	private List<Player> giocatori;
	private Graph<Player, DefaultWeightedEdge> grafo;
	private Map<Integer, Player> mappa;
	private List<Player> best;
	private double max;
	
	
	public Model() {
		this.dao = new PremierLeagueDAO();
		this.giocatori = new ArrayList<>();
		this.grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		this.mappa = new HashMap<>();
	}
	
	public void creaGrafo(double media) {
		giocatori = dao.listPlayers(media);
		Graphs.addAllVertices(this.grafo, giocatori);
		for (Player p: dao.listAllPlayers()) {
			mappa.put(p.playerID, p);
		}
		for (Arco a : dao.getArchi(mappa)) {
			if(grafo.containsVertex(a.getP1()) && grafo.containsVertex(a.getP2())) {
				if(a.getPeso() < 0) {
					//arco da p2 a p1
					Graphs.addEdgeWithVertices(grafo, a.getP2(), a.getP1(), ((double) -1)*a.getPeso());
				} else if(a.getPeso() > 0){
					//arco da p1 a p2
					Graphs.addEdgeWithVertices(grafo, a.getP1(), a.getP2(), a.getPeso());
				}
			}
		}
	}
	public int getV() {
		return this.grafo.vertexSet().size();
	}
	
	public int getA() {
		return this.grafo.edgeSet().size();
	}
	
	public List<Arco> cercaTop() {
		Set<DefaultWeightedEdge> lista = new HashSet<>();
		List<Arco> result = new ArrayList<>();
		Player top = null;
		int max =0;
		for (Player p: this.grafo.vertexSet()) {
			int uscenti = this.grafo.outDegreeOf(p);
			if (uscenti >max) {
				max =uscenti;
				top = p;
			}
		}
		lista = this.grafo.outgoingEdgesOf(top);
		for (DefaultWeightedEdge e : lista) {
			result.add(new Arco(top, this.grafo.getEdgeTarget(e), (int) this.grafo.getEdgeWeight(e)));
		}
		return result;
		
	}

	public void getDreamTeam(int k) {
		List<Player> parziale = new ArrayList<>();
		this.best = new ArrayList<>();
		this.max =0;
		List<Player> disponibili = new ArrayList<>(this.grafo.vertexSet());
		for (Player p: this.grafo.vertexSet()) {
			parziale.add(p);
			ricorsione(parziale, disponibili, k );
			parziale.remove(parziale.size()-1);
		}
	}

	private void ricorsione(List<Player> parziale, List<Player> disponibili, int k) {
		//condizione di uscita
		if (parziale.size()== k) {
			if (calcolaGrado(parziale)>= max) {
				max = calcolaGrado(parziale);
				this.best = new ArrayList<>(parziale);
			}
			return;
		}
		 
		
		//condizione normale 
		for (Player p: disponibili) {
			parziale.add(p);
			List<Player> nuoviDisponibili = trovaNuoviDisponibili(p, disponibili);
			ricorsione(parziale, nuoviDisponibili, k);
			parziale.remove(parziale.size()-1);
		}
		
	}

	private List<Player> trovaNuoviDisponibili(Player p, List<Player> disponibili) {
		List<Player> daTogliere = new ArrayList<>();
		List<Player> nuovi = new ArrayList<>(disponibili);
		Set<DefaultWeightedEdge> e = this.grafo.incomingEdgesOf(p);
		for (DefaultWeightedEdge d: e) {
			daTogliere.add(this.grafo.getEdgeSource(d));
		}
		for (Player a: daTogliere) {
			if (nuovi.contains(a)) {
				nuovi.remove(a);
			}
		}
		
		return nuovi;
	}

	private double calcolaGrado(List<Player> parziale) {
		double grado =0;
		for (Player p: parziale) {
			Set<DefaultWeightedEdge> uscenti = this.grafo.outgoingEdgesOf(p);
			Set<DefaultWeightedEdge> entranti = this.grafo.incomingEdgesOf(p);
			double u = calcolaPeso(uscenti);
			double e = calcolaPeso(entranti);
			grado+= (u-e);
		}
		return grado;
	}

	private double calcolaPeso(Set<DefaultWeightedEdge> lista) {
		double peso =0;
		for (DefaultWeightedEdge e : lista) {
			peso+= this.grafo.getEdgeWeight(e);
		}
		return peso;
	}
	
	

}
