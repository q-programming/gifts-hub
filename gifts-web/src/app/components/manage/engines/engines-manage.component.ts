import {Component, Inject, OnInit} from '@angular/core';
import {AlertService} from "@services/alert.service";
import {ApiService} from "@services/api.service";
import {AppSettings} from "@model/AppSettings";
import {environment} from "@env/environment";
import {FormArray, FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {DOCUMENT} from "@angular/common";
import {SearchEngine} from "@model/SearchEngine";
import * as _ from "lodash";

@Component({
  selector: 'manage-engines',
  templateUrl: './engines-manage.component.html',
  styles: []
})
export class EngineManageComponent implements OnInit {

  form: FormGroup;
  settings: AppSettings = new AppSettings();

  constructor(private alertSrv: AlertService, private apiSrv: ApiService, private formBuilder: FormBuilder, @Inject(DOCUMENT) private document: Document) {
    this.form = this.formBuilder.group({
      engines: this.formBuilder.array([])
    })
  }

  ngOnInit() {
    this.apiSrv.getObject<AppSettings>(`${environment.app_url}/settings`).subscribe(result => {
      this.settings = result;
      this.setEngines();
    })
  }


  saveConfiguration() {
    if (this.form.valid) {
      this.getSettings();
      this.apiSrv.put(`${environment.app_url}/settings`, this.settings).subscribe(() => {
        this.alertSrv.success('app.manage.saved');
      }, error => {
        this.alertSrv.error('app.manage.error');
      })
    }
  }

  get EnginesControls() {
    return (this.form.get('engines') as FormArray).controls
  }

  private getSettings() {
    this.settings.searchEngines = _.map(this.EnginesControls, ctrl => {
      return {
        id: ctrl.get('id').value,
        name: ctrl.get('name').value,
        icon: ctrl.get('icon').value,
        searchString: ctrl.get('searchString').value
      }
    });
  }

  private setEngines() {
    if (this.settings.searchEngines.length > 0) {
      let that = this;
      this.settings.searchEngines.forEach((engine) => {
        const enginesForm = <FormArray>that.form.controls['engines'];
        enginesForm.push(this.addEngine(engine));
        that.form.addControl(engine.name, new FormControl(true))
      })
    }
  }

  newEngine() {
    const enginesForm = <FormArray>this.form.controls['engines'];
    enginesForm.push(this.addEngine(new SearchEngine()));
  }
  removeEngine(i){
    const control = <FormArray>this.form.controls['engines'];
    control.removeAt(i);
  }

  addEngine(engine: SearchEngine) {
    return this.formBuilder.group({
      name: [engine.name],
      id: [engine.id],
      icon: [engine.icon],
      searchString: [engine.searchString]
    });
  }
}
