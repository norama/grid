

    const SERVICE = '/Grid/webresources/app';
    
    const NEXT = {
            'free': 'reserve',
            'reserved': 'take',
            'taken': 'free'
        };
    
    const STATE = {
        0: 'free',
        1: 'reserved',
        2: 'taken'
    };
    
    Vue.component('grid-cell', {
      props: ['NEXT', 'SERVICE', 'STATE', 'cell', 'errors'],
      template: '<div v-bind:class="{ disabled: disabled }"> \
      <free-cell-content v-if="state === \'free\'" v-bind:cell="cell" v-bind:next="next" v-bind:free="free"> \
      </free-cell-content> \
      <cell-content v-else-if="state === \'reserved\'" v-bind:cell="cell" v-bind:next="next" v-bind:state="state" inputField="owner" inputDescription="Owner" actionLabel="Take" uiInit="expanded" v-bind:cancel="free"> \
      </cell-content> \
      <cell-content v-else-if="state === \'taken\'" v-bind:cell="cell" v-bind:next="next" v-bind:state="state" inputField="ticket" inputDescription="Ticket" actionLabel="Free" uiInit="collapsed"> \
      </cell-content> \
      </div>',
      
      data: function () {
        return {
            disabled: false,
            errors: null
        };
      },
      computed: {
          state: function() {
              return STATE[this.cell.status];
          }
      },
      methods: {
        next: function(successCallback, failureCallback) {

            const action = NEXT[this.state];
            this.serve(action, successCallback, failureCallback);
 
        },
        free: function(successCallback, failureCallback) {
            this.serve('free', successCallback, failureCallback);
        },
        serve: function(action, successCallback, failureCallback) {
            this.disabled = true;
            this.errors = null;
            Vue.http.post(SERVICE + '/' + action,
                           this.cell, {
                                headers: {
                                    'Content-Type': 'application/json'
                                }
                            }).then(response => {
                                console.dir(response); 
                                this.disabled = false;
                                this.cell = response.body;
                                console.dir(this.cell);
                                if (action == 'take') {
                                    self = this;
                                    setTimeout(function () { 
                                        showInfo('Ticket: '+self.cell.ticket); 
                                    }, 500);
                                } 
                                if (successCallback) {
                                    successCallback();
                                }
                            }, response => { 
                                console.dir(response); 
                                this.disabled = false;
                                this.errors = response.body.message;
                                const self = this;
                                setTimeout(function () { 
                                    showError(self.errors); 
                                }, 500);
                                if (failureCallback) {
                                    failureCallback(this.errors);
                                }
                            });            
        }
      }
    });
    
    Vue.component('free-cell-content', {
      props: ['cell', 'next'],
      template: '<button class="grid-cell free" v-on:click="next()" v-bind:title="\'Reserve cell \' + cell.id"></button>'
    });

    Vue.component('cell-content', {
      props: {
          cell: {
              type: Object,
              required: true
          }, 
          inputField: {
              type: String,
              required: true
          }, 
          inputDescription: {
              type: String,
              required: true
          }, 
          actionLabel: {
              type: String,
              default: 'Next'
          }, 
          uiInit: {
              type: String,
              default: 'collapsed'  
          }, 
          next: {
              type: Function,
              required: true
          }, 
          errors: {
              type: String,
              default: null
          }, 
          state: {
              type: String,
              required: true
          },
          cancel: {
              type: Function,
              default: function() {
                  this.collapse();
              }
          }
      },
      template: '<button v-if="ui === \'collapsed\'" v-on:click="expand()" v-bind:class="[\'grid-cell\', state, \'collapsed\']" v-bind:title="actionLabel + \' cell \' + cell.id">\
      <div class="collapsed" v-text="cell.owner"></div>\
      </button>\
      <div v-else-if="ui === \'expanded\'" v-bind:class="[\'grid-cell\', state, \'expanded\']">\
      <a class="x" title="Cancel" v-on:click="cancel()">&times;</a>\
      <form>\
      <input type="text" v-model="userInput" v-bind:class="{ \'input-error\': errors }" name="userInput" size="8" :placeholder="inputDescription"><br>\
      <div><button type="button" @click="act()" v-bind:class="{ disabled: disabled }">{{ actionLabel }}</button>\
      </div>\
      </form></div>',
      data: function() {
          return {
            disabled: false,
            ui: this.uiInit, 
            userInput: null
          };
      },
      methods: {
          expand: function(errors) {
              this.ui = 'expanded';
              this.errors = errors;
          },
          collapse: function() {
              this.userInput = null;
              this.ui = 'collapsed';
          },
          act: function() {
              this.errors = null;
              if (!this.userInput) {
                  const self = this;
                  self.disabled = true;
                  self.errors = this.inputDescription + ' should not be empty!';
                  setTimeout(function () { 
                      showError(self.errors); 
                      self.disabled = false;
                  }, 500);
                  return;
              }
              this.cell[this.inputField] = this.userInput;
              this.next(this.collapse, this.expand);
          }
      }
    });
    
    let vm = new Vue({
      data: {
        nrows: 4,
        ncols: 3,
        cells: null
      },
      //el: '#grid',
      created: function() {
        Vue.http.get(SERVICE).then(
        response => {
            console.dir(response); 
            //alert('success: '+JSON.stringify(response.body.cells));
            this.init(response.body.cells);
            this.$mount('#grid'); 
        }, response => { 
            console.dir(response); 
            showError(response.body.message); 
        });
         
      },
      methods: {
        init(dbcells) {
            this.nrows = dbcells.reduce(function(value, cell) {
                return Math.max(value, cell.row);
            }, 0);
            this.ncols = dbcells.reduce(function(value, cell) {
                return Math.max(value, cell.col);
            }, 0);
            this.cells = new Array(this.nrows + 1);
            for (let i=1; i <= this.nrows; ++i) {
                this.cells[i] = new Array(this.ncols + 1);
            }
            for (let dbcell of dbcells) {
                if (dbcell.status === 1) {
                    // replace 'reserved' by 'taken'
                    dbcell.status = 2; 
                }
                this.cells[dbcell.row][dbcell.col] = dbcell;
            }
        }    
      },
      template: '<table> \
        <tr v-for="r in nrows"> \
          <td v-for="c in ncols"> \
              <grid-cell v-bind:id="cells[r][c].id" v-bind:cell="cells[r][c]"></grid-cell> \
          </td> \
        </tr> \
        </table>'
    });
