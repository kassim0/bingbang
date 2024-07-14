import {RouterModule, Routes} from '@angular/router';
import {TestApiComponent} from "./components/test-api/test-api.component";
import {NgModule} from "@angular/core";
import {HttpClientModule} from "@angular/common/http";
import {ApiModule, Configuration} from "./openApi";
import {CommonModule} from "@angular/common";
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';

export const routes: Routes = [
  {path: 'api', component: TestApiComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes),
    HttpClientModule,
    CommonModule,
    MatFormFieldModule,
    FormsModule,
    ApiModule.forRoot(() => new Configuration({basePath: 'http://localhost:8080'}))
  ],
  exports: [RouterModule]
})
export class AppRoutingModule{}
