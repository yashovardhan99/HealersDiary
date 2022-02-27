package com.yashovardhan99.core.backup_restore


import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackupUtilsTest {

    @Test
    fun getCsvRow_empty() {
        val row = BackupUtils.getCsvRow()
        assertThat(row).isEmpty()
    }

    @Test
    fun getCsvRow_single() {
        val row = BackupUtils.getCsvRow("Test")
        assertThat(row).isEqualTo("Test")
    }

    @Test
    fun getCsvRow_quotesString() {
        val row = BackupUtils.getCsvRow("\"Test", "Test2", "Test3,")
        assertThat(row).isEqualTo("\"\"\"Test\",Test2,\"Test3,\"")
    }
}