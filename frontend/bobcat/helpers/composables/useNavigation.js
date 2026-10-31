export default function useNavigation() {
  const filterHiddenNavigationItems = (items) => {
    return items
      .filter(item => !item?.hideFromNavigation)
      .map(item => ({
        ...item,
        itemsFiltered: filterHiddenNavigationItems(item.items || [])
      }))
  }

  const filterHiddenMobileNavigationItems = (items) => {
    return items
      .filter((item) => {
        return !item?.hideFromNavigation
      })
      .map(item => ({
        ...item,
        itemsFiltered: filterHiddenMobileNavigationItems(item.items || []),
        rowsFiltered: item.rows?.map(row => ({
          ...row,
          columnsFiltered: row.columns?.filter((col) => {
            return !col?.hideFromNavigation
          }).map(col => ({
            ...col,
            itemsFiltered: filterHiddenMobileNavigationItems(col.items || [])
          })) || []
        })) || []
      }))
  }

  return {
    filterHiddenNavigationItems,
    filterHiddenMobileNavigationItems
  }
}
