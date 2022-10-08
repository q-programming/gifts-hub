import {Component, EventEmitter, Inject, Input, OnInit, Output} from '@angular/core';
import {AlertService} from "@core-services/alert.service";
import {ApiService} from "@core-services/api.service";
import {AppSettings} from "@model/AppSettings";
import {environment} from "@env/environment";
import {UntypedFormArray, UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {DOCUMENT} from "@angular/common";
import {SearchEngine} from "@model/SearchEngine";
import * as _ from "lodash";

@Component({
  selector: 'manage-engines',
  templateUrl: './engines-manage.component.html',
  styles: []
})
export class EngineManageComponent implements OnInit {

  form: UntypedFormGroup;
  @Input() settings: AppSettings;
  @Output() commit : EventEmitter<boolean> = new EventEmitter();

  constructor(private alertSrv: AlertService, private apiSrv: ApiService, private formBuilder: UntypedFormBuilder, @Inject(DOCUMENT) private document: Document) {
    this.form = this.formBuilder.group({
      engines: this.formBuilder.array([])
    })
  }

  ngOnInit() {
    this.setEngines();
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
    return (this.form.get('engines') as UntypedFormArray).controls
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
        const enginesForm = <UntypedFormArray>that.form.controls['engines'];
        enginesForm.push(this.addEngine(engine));
        that.form.addControl(engine.name, new UntypedFormControl(true))
      })
    }
  }

  newEngine() {
    const enginesForm = <UntypedFormArray>this.form.controls['engines'];
    enginesForm.push(this.addEngine(new SearchEngine()));
  }

  removeEngine(i) {
    const control = <UntypedFormArray>this.form.controls['engines'];
    control.removeAt(i);
  }

  addEngine(engine: SearchEngine) {
    return this.formBuilder.group({
      name: [engine.name, Validators.required],
      id: [engine.id],
      icon: [engine.icon, Validators.required],
      searchString: [engine.searchString, Validators.required]
    });
  }
}
