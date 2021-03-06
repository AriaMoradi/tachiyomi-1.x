/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AmbientContentColor
import androidx.compose.material.Checkbox
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TriStateCheckbox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientAnimationClock
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import tachiyomi.domain.library.model.DisplayMode
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibraryFilter.Value.Excluded
import tachiyomi.domain.library.model.LibraryFilter.Value.Included
import tachiyomi.domain.library.model.LibraryFilter.Value.Missing
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.ui.core.components.ChoiceChip
import tachiyomi.ui.core.components.Pager
import tachiyomi.ui.core.components.PagerState
import tachiyomi.ui.core.theme.CustomColors
import tachiyomi.ui.core.viewmodel.viewModel
import java.util.Locale

@Composable
fun LibrarySheet() {
  val vm = viewModel<LibrarySheetViewModel>()

  val clock = AmbientAnimationClock.current
  val selectedPage = vm.selectedPage
  val state = remember(selectedPage) {
    PagerState(clock, selectedPage, 0, 2)
  }
  onCommit(state.currentPage) {
    if (selectedPage != state.currentPage) {
      vm.selectedPage = state.currentPage
    }
  }

  TabRow(
    modifier = Modifier.height(48.dp),
    selectedTabIndex = selectedPage,
    backgroundColor = CustomColors.current.bars,
    contentColor = CustomColors.current.onBars
  ) {
    listOf("Filter", "Sort", "Display").forEachIndexed { i, title ->
      Tab(selected = selectedPage == i, onClick = { vm.selectedPage = i }) {
        Text(title)
      }
    }
  }
  Pager(state = state) {
    ScrollableColumn(modifier = Modifier.fillMaxSize()) {
      when (page) {
        0 -> FiltersPage(filters = vm.filters, onClick = { vm.toggleFilter(it) })
        1 -> SortPage(sorting = vm.sorting, onClick = { vm.toggleSort(it) })
        2 -> DisplayPage(
          displayMode = vm.displayMode,
          downloadBadges = vm.downloadBadges,
          unreadBadges = vm.unreadBadges,
          categoryTabs = vm.showCategoryTabs,
          allCategory = vm.showAllCategory,
          onDisplayModeClick = { vm.changeDisplayMode(it) },
          onDownloadBadgesClick = { vm.toggleDownloadBadges() },
          onUnreadBadgesClick = { vm.toggleUnreadBadges() },
          onCategoryTabsClick = { vm.toggleShowCategoryTabs() },
          onAllCategoryClick = { vm.toggleShowAllCategory() }
        )
      }
    }
  }
}

@Composable
private fun FiltersPage(
  filters: List<LibraryFilter>,
  onClick: (LibraryFilter.Type) -> Unit
) {
  filters.forEach { (filter, state) ->
    ClickableRow(onClick = { onClick(filter) }) {
      TriStateCheckbox(
        modifier = Modifier.padding(horizontal = 16.dp),
        state = state.asToggleableState(),
        onClick = { onClick(filter) }
      )
      Text(filter.name)
    }
  }
}

@Composable
private fun SortPage(
  sorting: LibrarySort,
  onClick: (LibrarySort.Type) -> Unit
) {
  LibrarySort.types.forEach { type ->
    ClickableRow(onClick = { onClick(type) }) {
      val iconModifier = Modifier.width(56.dp)
      if (sorting.type == type) {
        val icon = if (sorting.isAscending) {
          Icons.Default.KeyboardArrowUp
        } else {
          Icons.Default.KeyboardArrowDown
        }
        Icon(icon, iconModifier, MaterialTheme.colors.primary)
      } else {
        Spacer(iconModifier)
      }
      Text(type.name)
    }
  }
}

@OptIn(ExperimentalLayout::class)
@Composable
private fun DisplayPage(
  displayMode: DisplayMode,
  downloadBadges: Boolean,
  unreadBadges: Boolean,
  categoryTabs: Boolean,
  allCategory: Boolean,
  onDisplayModeClick: (DisplayMode) -> Unit,
  onDownloadBadgesClick: () -> Unit,
  onUnreadBadgesClick: () -> Unit,
  onCategoryTabsClick: () -> Unit,
  onAllCategoryClick: () -> Unit
) {
  Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
    Text(
      text = "Display mode".toUpperCase(Locale.ROOT),
      modifier = Modifier.padding(bottom = 12.dp),
      style = MaterialTheme.typography.subtitle2,
      color = AmbientContentColor.current.copy(alpha = ContentAlpha.medium)
    )
    FlowRow(mainAxisSpacing = 4.dp) {
      DisplayMode.values.forEach {
        ChoiceChip(
          isSelected = it == displayMode,
          onClick = { onDisplayModeClick(it) },
          content = { Text(it.name) }
        )
      }
    }
  }
  Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
    Text(
      text = "Badges".toUpperCase(Locale.ROOT),
      modifier = Modifier.padding(bottom = 12.dp),
      style = MaterialTheme.typography.subtitle2,
      color = AmbientContentColor.current.copy(alpha = ContentAlpha.medium)
    )
    FlowRow(mainAxisSpacing = 4.dp) {
      ChoiceChip(
        isSelected = unreadBadges,
        onClick = { onUnreadBadgesClick() },
        content = { Text("Unread") }
      )
      ChoiceChip(
        isSelected = downloadBadges,
        onClick = { onDownloadBadgesClick() },
        content = { Text("Downloaded") }
      )
    }
  }
  Text(
    text = "Tabs".toUpperCase(Locale.ROOT),
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    style = MaterialTheme.typography.subtitle2,
    color = AmbientContentColor.current.copy(alpha = ContentAlpha.medium)
  )
  ClickableRow(onClick = { onCategoryTabsClick() }) {
    Checkbox(
      modifier = Modifier.padding(horizontal = 16.dp),
      checked = categoryTabs,
      onCheckedChange = { onCategoryTabsClick() }
    )
    Text("Show category tabs")
  }
  ClickableRow(onClick = { onAllCategoryClick() }) {
    Checkbox(
      modifier = Modifier.padding(horizontal = 16.dp),
      checked = allCategory,
      onCheckedChange = { onAllCategoryClick() }
    )
    Text("Show all category")
  }
}

@Composable
private fun ClickableRow(onClick: () -> Unit, content: @Composable () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().height(48.dp).clickable(onClick = { onClick() }),
    verticalAlignment = Alignment.CenterVertically,
    content = { content() }
  )
}

private fun LibraryFilter.Value.asToggleableState(): ToggleableState {
  return when (this) {
    Included -> ToggleableState.On
    Excluded -> ToggleableState.Indeterminate
    Missing -> ToggleableState.Off
  }
}
