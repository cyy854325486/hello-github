package com.example.hellogithub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hellogithub.R
import com.example.hellogithub.data.Repository

class RepositoryAdapter(private var repos: List<Repository>) : RecyclerView.Adapter<RepositoryAdapter.RepositoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_repository, parent, false)
        return RepositoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        val repo = repos[position]
        holder.bind(repo)
    }

    override fun getItemCount() = repos.size

    fun updateData(newRepos: List<Repository>) {
        repos = newRepos
        notifyDataSetChanged()
    }

    class RepositoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val repoName: TextView = itemView.findViewById(R.id.repoName)
        private val repoDescription: TextView = itemView.findViewById(R.id.repoDescription)
        private val ownerAvatar: ImageView = itemView.findViewById(R.id.ownerAvatar)

        fun bind(repo: Repository) {
            repoName.text = repo.name
            repoDescription.text = repo.description ?: "No description available"
            Glide.with(itemView.context).load(repo.owner.avatarUrl).into(ownerAvatar)
        }
    }
}
