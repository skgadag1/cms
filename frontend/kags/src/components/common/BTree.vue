<template>
  <div>
    <div v-for="item in items" :key="item.id" class="vue-tree">
      <ul>
        <b-tree-item :node="item" :code="item.code || '_'" />
      </ul>
    </div>
  </div>
</template>

<script>
import BTreeItem from './BTreeItem'
export default {
  name: 'BTree',
  components: {
    BTreeItem
  },
  props: {
    items: {
      type: Array
    }
  }
}
</script>

<style lang="scss">
@import '../../scss/transitions.scss';
@import '../../scss/colors.scss';
@import "../../scss/grid";

.vue-tree {
  ul {
    margin: 0;
    padding-left: 0;

    ul {
      padding-left: 1.5rem;
      margin-left: -1rem;
    }

    li {
      margin: 0;
      position: relative;
      list-style: none;
      padding-left: 1rem;
      padding-top: .4rem;

      .node-name {
        padding-bottom: .5rem;
        margin-left: -1.5rem;
        padding-left: 1.5rem;

        &:hover {
          background-color: $hover-default;
          transition: $transition-base;
        }

        > div {

          &::before {
            display: inline-block;
            content: "";
            border-left: 0.3em solid;
            border-top: 0.3em solid transparent;
            border-right: 0;
            border-bottom: 0.3em solid transparent;
            transition: $transition-base;
            position: absolute;
            top: .8rem;
            left: -.1rem
          }
        }
      }

      > div.is-open >div::before {
        transform: rotate(90deg);
      }
    }

    .collapsing li {
      &::after {
        height: 0px;
      }
    }

    li {
      &::after,
      &::before {
        content: '';
        left: -1.5rem;
        position: absolute;
        right: auto
      }

      &::before {
        border-left: .0125rem solid $gray-700;
        height: 100%;
        top: .4rem;
        width: .0125rem;
      }

      &::after {
        border-top: .0125rem solid $gray-700;
        height: 2rem;
        top: 1.1rem;
        width: .7rem
      }

      &:last-child::before {
        height: .7rem;
      }
    }
  }

  & > ul > li::after,
  & > ul > li:before {
    border: 0
  }


  > ul > li { //first items
    border-bottom: none;
    > .node-name {
      font-size: 1.125rem;
      border-bottom: none;
    }

    > hr {
      display: none;
    }


    > ul > li > ul > li {
      font-size: 0.875rem;

      &:last-child {
        > hr {
          display: none;
        }
      }

      .node-details {
        font-size: 0.75rem;
        width: 100%;

        @include media-breakpoint-up('md') {
          .component-header {
            width: 25%;
          }
        }

        tr {
          border-bottom: 1px solid $gray-400;

          td {
            vertical-align: top;
            width: 25%;
            border-left: 1px solid $gray-400;
          }

          td:first-child {
            border-left: none;
          }

          td:last-child {
            width: auto;
          }
        }
      }
    }
  }
}
</style>
