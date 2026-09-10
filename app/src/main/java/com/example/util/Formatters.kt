package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.TransactionEntity
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {
    private val ptBrLocale = Locale("pt", "BR")

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(ptBrLocale)
        // Ensure standard R$ prefix with proper space
        return format.format(amount).replace("R$", "R$ ")
    }

    fun parseCurrencyInput(text: String): Double {
        if (text.isBlank()) return 0.0
        val clean = text.replace("R$", "")
            .replace(" ", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", ptBrLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd 'de' MMM", ptBrLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateHeader(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE, dd 'de' MMMM", ptBrLocale)
        val formatted = sdf.format(Date(timestamp))
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptBrLocale) else it.toString() }
    }

    fun formatMonthYear(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM 'de' yyyy", ptBrLocale)
        val formatted = sdf.format(Date(timestamp))
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptBrLocale) else it.toString() }
    }

    fun isToday(timestamp: Long): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isSameDay(t1: Long, t2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isThisMonth(timestamp: Long): Boolean {
        val calNow = Calendar.getInstance()
        val calTarget = Calendar.getInstance().apply { timeInMillis = timestamp }
        return calNow.get(Calendar.YEAR) == calTarget.get(Calendar.YEAR) &&
                calNow.get(Calendar.MONTH) == calTarget.get(Calendar.MONTH)
    }

    fun isPreviousMonth(timestamp: Long): Boolean {
        val calNow = Calendar.getInstance()
        calNow.add(Calendar.MONTH, -1)
        val calTarget = Calendar.getInstance().apply { timeInMillis = timestamp }
        return calNow.get(Calendar.YEAR) == calTarget.get(Calendar.YEAR) &&
                calNow.get(Calendar.MONTH) == calTarget.get(Calendar.MONTH)
    }

    fun isThisWeek(timestamp: Long): Boolean {
        val calNow = Calendar.getInstance()
        val calTarget = Calendar.getInstance().apply { timeInMillis = timestamp }
        return calNow.get(Calendar.YEAR) == calTarget.get(Calendar.YEAR) &&
                calNow.get(Calendar.WEEK_OF_YEAR) == calTarget.get(Calendar.WEEK_OF_YEAR)
    }

    fun exportTransactionsCsv(context: Context, transactions: List<TransactionEntity>) {
        val sb = StringBuilder()
        sb.append("ID,Data,Tipo,Descrição,Categoria,Valor (R$),Forma de Pagamento,Fixo/Variável,Observação\n")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", ptBrLocale)
        for (tx in transactions) {
            val dateStr = dateFormat.format(Date(tx.date))
            val tipoStr = if (tx.type == "INCOME") "Receita" else "Despesa"
            val fixoStr = if (tx.isFixed) "Fixa" else "Variável"
            val safeTitle = tx.title.replace(",", " ")
            val safeCat = tx.category.replace(",", " ")
            val safeNote = tx.note.replace(",", " ").replace("\n", " ")
            val safePayment = tx.paymentMethod.replace(",", " ")
            val valorStr = String.format(Locale.US, "%.2f", tx.amount)

            sb.append("${tx.id},$dateStr,$tipoStr,\"$safeTitle\",\"$safeCat\",$valorStr,\"$safePayment\",$fixoStr,\"$safeNote\"\n")
        }

        try {
            val fileName = "bgs_na_linha_extrato_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(sb.toString(), Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Extrato Financeiro - BGS NA LINHA")
                putExtra(Intent.EXTRA_TEXT, "Segue em anexo o extrato financeiro exportado do aplicativo BGS NA LINHA.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Exportar Extrato CSV"))
        } catch (e: Exception) {
            // Fallback plain text share if FileProvider is not set up
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Extrato BGS NA LINHA")
                putExtra(Intent.EXTRA_TEXT, sb.toString())
            }
            context.startActivity(Intent.createChooser(sendIntent, "Exportar Extrato"))
        }
    }
}
