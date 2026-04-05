import {Component, OnInit} from '@angular/core';
import {SearchBarComponent} from "../share/search-bar/search-bar.component";
import {ListGamesPopupComponent} from "../list-games-popup/list-games-popup.component";
import {MatDialog} from "@angular/material/dialog";
import {MatList, MatListItem} from "@angular/material/list";
import {MatDivider} from "@angular/material/divider";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    SearchBarComponent,
    ListGamesPopupComponent,
    MatListItem,
    MatDivider,
    MatList,
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit{

  backgroundImage: string = 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)';
  isDragging = false;
  hasCustomImage = false;

  constructor(public dialog:MatDialog) {
  }

  ngOnInit(): void {
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent) {
    this.isDragging = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragging = false;

    const file = event.dataTransfer?.files[0];
    if (!file || !file.type.startsWith('image/')) return;

    const reader = new FileReader();
    reader.onload = () => {
      this.backgroundImage = `url('${reader.result}')`;
      this.hasCustomImage = true;
    };
    reader.readAsDataURL(file);
  }

  OpenPopup() {
    this.dialog.open(ListGamesPopupComponent,{
      width: '40%',
      height:'90%',
      data: {}
    })
  }


}
